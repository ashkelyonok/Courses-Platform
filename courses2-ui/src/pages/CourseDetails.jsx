import { useAuth } from '../context/AuthContext';
import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  getCourseById,
  updateCourse,
  deleteCourse,
  getAllUsers,
  removeStudentFromCourse,
  addStudentToCourse,
  unassignInstructorFromCourse,
  getUserById,
  getEnrolledCourses,
  getTaughtCourses,
} from '../services/api';
import { Descriptions, Button, Form, Input, Select, message, Spin, Alert, Card, Modal, List } from 'antd';
import { EditOutlined } from '@ant-design/icons';
import styles from '../styles/CourseDetails.module.css';

const { Option } = Select;

function CourseDetails() {
  const { user } = useAuth();
  const { id } = useParams();
  const navigate = useNavigate();
  const [course, setCourse] = useState(null);
  const [instructors, setInstructors] = useState([]);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isAddStudentModalOpen, setIsAddStudentModalOpen] = useState(false);
  const [isUserDetailsModalOpen, setIsUserDetailsModalOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState(null);
  const [userDetails, setUserDetails] = useState(null);
  const [userEnrolledCourses, setUserEnrolledCourses] = useState([]);
  const [userTaughtCourses, setUserTaughtCourses] = useState([]);
  const [userLoading, setUserLoading] = useState(false);
  const [userError, setUserError] = useState(null);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [confirmData, setConfirmData] = useState(null);
  const [form] = Form.useForm();
  const [addStudentForm] = Form.useForm();

  const fetchCourseData = async () => {
    try {
      setLoading(true);
      const response = await getCourseById(id);
      setCourse(response.data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load course data');
      console.error('Course fetch error:', {
        message: err.message,
        status: err.response?.status,
        data: err.response?.data,
      });
    } finally {
      setLoading(false);
    }
  };

  const fetchUserData = async (userId) => {
    try {
      setUserLoading(true);
      const userResponse = await getUserById(userId);
      const fetchedUser = userResponse.data;
      if (!fetchedUser || !fetchedUser.id) {
        throw new Error('Invalid user data');
      }
      setUserDetails({
        id: fetchedUser.id,
        name: fetchedUser.name || 'Unknown User',
        email: fetchedUser.email || 'Not provided',
        role: fetchedUser.role || 'Unknown',
      });

      if (fetchedUser.role === 'STUDENT' || fetchedUser.role === 'ADMIN') {
        const enrolledResponse = await getEnrolledCourses(userId);
        setUserEnrolledCourses(enrolledResponse.data || []);
      } else {
        setUserEnrolledCourses([]);
      }

      if (fetchedUser.role === 'INSTRUCTOR' || fetchedUser.role === 'ADMIN') {
        const taughtResponse = await getTaughtCourses(userId);
        setUserTaughtCourses(taughtResponse.data || []);
      } else {
        setUserTaughtCourses([]);
      }

      setUserError(null);
    } catch (err) {
      setUserError(err.response?.data?.message || 'Failed to load user data');
      console.error('Fetch user error:', {
        message: err.message,
        status: err.response?.status,
        data: err.response?.data,
      });
      setUserDetails(null);
    } finally {
      setUserLoading(false);
    }
  };

  useEffect(() => {
    fetchCourseData();
  }, [id]);

  useEffect(() => {
    if (isEditModalOpen || isAddStudentModalOpen) {
      getAllUsers().then((res) => {
        setInstructors(res.data.filter((u) => u.role === 'INSTRUCTOR'));
        setStudents(res.data.filter((u) => u.role === 'STUDENT'));
      });
    }
  }, [isEditModalOpen, isAddStudentModalOpen]);

  useEffect(() => {
    if (isUserDetailsModalOpen && selectedUserId) {
      fetchUserData(selectedUserId);
    }
  }, [isUserDetailsModalOpen, selectedUserId]);

  const formatStatus = (status) => {
    if (!status) return 'Unknown';
    switch (status.toUpperCase()) {
      case 'ACTIVE':
        return 'Active';
      case 'PENDING_INSTRUCTOR':
        return 'Pending Instructor';
      default:
        return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
    }
  };

  const handleUpdate = async (values) => {
    try {
      setUpdating(true);
      await updateCourse(course.id, {
        name: values.name,
        description: values.description,
        instructorId: values.instructorId || null,
      });
      await fetchCourseData();
      setIsEditModalOpen(false);
      message.success('Course updated successfully');
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to update course');
      console.error('Course update error:', {
        message: err.message,
        status: err.response?.status,
        data: err.response?.data,
      });
    } finally {
      setUpdating(false);
    }
  };

  const handleDelete = async () => {
    setConfirmAction('delete');
    setConfirmData({ courseName: course?.name || 'this course' });
    setIsConfirmModalOpen(true);
  };

  const handleUnenroll = (student) => {
    setConfirmAction('unenroll');
    setConfirmData({ student, courseName: course?.name || 'this course' });
    setIsConfirmModalOpen(true);
  };

  const handleUnassign = () => {
    setConfirmAction('unassign');
    setConfirmData({ instructorName: course?.instructor?.name || 'the instructor', courseName: course?.name || 'this course' });
    setIsConfirmModalOpen(true);
  };

  const handleEnroll = async () => {
    if (!user?.id) {
      message.error('User not authenticated. Please log in again.');
      return;
    }
    try {
      await addStudentToCourse(course.id, user.id);
      message.success('Enrolled successfully');
      // await fetchCourseData();
      setTimeout(() => fetchCourseData(), 500);
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to enroll');
      console.error('Enroll error:', {
        message: err.message,
        status: err.response?.status,
        data: err.response?.data,
      });
    }
  };

  const handleSelfUnenroll = async () => {
    setConfirmAction('selfUnenroll');
    setConfirmData({ courseName: course?.name || 'this course' });
    setIsConfirmModalOpen(true);
  };

  const handleConfirmAction = async () => {
    try {
      if (confirmAction === 'delete') {
        await deleteCourse(course.id);
        message.success('Course deleted successfully');
        navigate('/profile');
      } else if (confirmAction === 'unenroll') {
        const { student } = confirmData;
        await removeStudentFromCourse(course.id, student.id);
        message.success('Student unenrolled successfully');
        // setCourse((prev) => ({
        //   ...prev,
        //   students: prev.students.filter((s) => s.id !== student.id),
        // }));
        // await fetchCourseData();
        setTimeout(() => {
        setCourse((prev) => ({
          ...prev,
          students: prev.students.filter((s) => s.id !== student.id),
        }));
        fetchCourseData();
      }, 500);
      } else if (confirmAction === 'unassign') {
        await unassignInstructorFromCourse(course.id, course.instructor.id);
        message.success('Instructor unassigned successfully');
        // setCourse((prev) => ({ ...prev, instructor: null }));
        // await fetchCourseData();
        setTimeout(() => {
        setCourse((prev) => ({ ...prev, instructor: null }));
        fetchCourseData();
      }, 500);
      } else if (confirmAction === 'selfUnenroll') {
        await removeStudentFromCourse(course.id, user.id);
        message.success('Unenrolled successfully');
        // setCourse((prev) => ({
        //   ...prev,
        //   students: prev.students.filter((s) => s.id !== user.id),
        // }));
        // await fetchCourseData();
        setTimeout(() => {
        setCourse((prev) => ({
          ...prev,
          students: prev.students.filter((s) => s.id !== user.id),
        }));
        fetchCourseData();
      }, 500);
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || `Failed to perform ${confirmAction} action`;
      message.error(errorMessage);
      console.error(`${confirmAction} error:`, {
        message: err.message,
        status: err.response?.status,
        data: err.response?.data,
      });
    } finally {
      setIsConfirmModalOpen(false);
      setConfirmAction(null);
      setConfirmData(null);
    }
  };

  const handleAddStudent = async (values) => {
    try {
      await addStudentToCourse(course.id, values.studentId);
      message.success('Student added successfully');
      // await fetchCourseData();
      // setIsAddStudentModalOpen(false);
      // addStudentForm.resetFields();
      setTimeout(() => {
      fetchCourseData();
      setIsAddStudentModalOpen(false);
      addStudentForm.resetFields();
    }, 500);
    } catch (err) {
      message.error('Failed to add student');
      console.error('Add student error:', {
        message: err.message,
        status: err.response?.status,
        data: err.response?.data,
      });
    }
  };

  const isAdmin = user?.role === 'ADMIN';
  const isInstructor = user?.role === 'INSTRUCTOR' && course?.instructor?.id === user?.id;
  const isStudent = user?.role === 'STUDENT';
  const isEnrolled = course?.students?.some((student) => student.id === user?.id);

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.spinner}>
          <Spin size="large" />
        </div>
      </div>
    );
  }

  if (error || !course) {
    return (
      <div className={styles.container}>
        <Alert
          message={error || 'Course not found'}
          type="error"
          showIcon
          action={
            <Button type="primary" onClick={() => navigate('/profile')} className={styles.backButton}>
              Back to Profile
            </Button>
          }
        />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.layout}>
        <div className={styles.courseSection}>
          <Card
            title={course.name}
            className={styles.courseCard}
            extra={
              (isAdmin || isInstructor) && (
                <Button
                  type="text"
                  icon={<EditOutlined />}
                  onClick={() => setIsEditModalOpen(true)}
                  className={styles.editButton}
                />
              )
            }
          >
            <Descriptions column={1} bordered className={styles.descriptions}>
              <Descriptions.Item label="Description" className={styles.descriptionItem}>
                {course.description}
              </Descriptions.Item>
              <Descriptions.Item label="Status">
                {formatStatus(course.courseStatus)}
              </Descriptions.Item>
            </Descriptions>
          </Card>
        </div>
        <div className={styles.sideSection}>
          <h3 className={styles.sectionTitle}>Instructor</h3>
          <Card className={styles.instructorCard}>
            {course.instructor ? (
              <div className={styles.userRow}>
                <div className={styles.userInfo}>
                  <h4 className={styles.userName}>
                    <a
                      onClick={() =>
                        course.instructor.id === user?.id
                          ? navigate('/profile')
                          : setSelectedUserId(course.instructor.id) || setIsUserDetailsModalOpen(true)
                      }
                      className={styles.userLink}
                    >
                      {course.instructor.name}
                    </a>
                  </h4>
                  <p className={styles.userEmail}>Email: {course.instructor.email}</p>
                </div>
                {(isAdmin || isInstructor) && course.instructor && (
                  <Button
                    onClick={handleUnassign}
                    className={styles.actionButton}
                  >
                    {isInstructor ? 'Unassign' : 'Unassign Instructor'}
                  </Button>
                )}
              </div>
            ) : (
              <p className={styles.noData}>No instructor assigned</p>
            )}
          </Card>
          <div className={styles.studentsHeader}>
            <h3 className={styles.sectionTitle}>Students</h3>
            {isAdmin && (
              <Button
                onClick={() => setIsAddStudentModalOpen(true)}
                className={styles.addStudentButton}
              >
                Add Student
              </Button>
            )}
          </div>
          {course.students && course.students.length > 0 ? (
            <div className={styles.studentList}>
              {course.students.map((student) => (
                <Card key={student.id} className={styles.studentCard}>
                  <div className={styles.userRow}>
                    <div className={styles.userInfo}>
                      <h4 className={styles.userName}>
                        <a
                          onClick={() =>
                            student.id === user?.id
                              ? navigate('/profile')
                              : setSelectedUserId(student.id) || setIsUserDetailsModalOpen(true)
                          }
                          className={styles.userLink}
                        >
                          {student.name}
                        </a>
                      </h4>
                      <p className={styles.userEmail}>Email: {student.email}</p>
                    </div>
                    {(isAdmin || isInstructor) && (
                      <Button
                        onClick={() => handleUnenroll(student)}
                        className={styles.actionButton}
                      >
                        Unenroll
                      </Button>
                    )}
                  </div>
                </Card>
              ))}
            </div>
          ) : (
            <p className={styles.noData}>No students enrolled</p>
          )}
        </div>
      </div>
      <div className={styles.actionSection}>
        <div className={styles.enrollSection}>
          {(isStudent || isAdmin) && !isInstructor && (
            <>
              {!isEnrolled ? (
                <Button
                  onClick={handleEnroll}
                  className={styles.enrollButton}
                >
                  Enroll
                </Button>
              ) : (
                <Button
                  onClick={handleSelfUnenroll}
                  className={styles.unenrollButton}
                >
                  Unenroll
                </Button>
              )}
            </>
          )}
        </div>
        {(isAdmin || isInstructor) && (
          <div className={styles.deleteSection}>
            <Button onClick={handleDelete} className={styles.deleteButton}>
              Delete Course
            </Button>
          </div>
        )}
      </div>
      <Modal
        title="Edit Course"
        open={isEditModalOpen}
        onCancel={() => setIsEditModalOpen(false)}
        footer={null}
        className={styles.modal}
      >
        <Form
          form={form}
          onFinish={handleUpdate}
          layout="vertical"
          initialValues={{
            name: course?.name,
            description: course?.description,
            instructorId: course?.instructor?.id,
          }}
          className={styles.form}
        >
          <Form.Item
            name="name"
            label="Name"
            rules={[{ required: true, max: 255, message: 'Name is required' }]}
          >
            <Input className={styles.input} />
          </Form.Item>
          <Form.Item
            name="description"
            label="Description"
            rules={[{ required: true, max: 1000, message: 'Description is required' }]}
          >
            <Input.TextArea className={styles.input} />
          </Form.Item>
          <Form.Item name="instructorId" label="Instructor">
            <Select allowClear placeholder="Select an instructor" className={styles.select}>
              {instructors.map((i) => (
                <Option key={i.id} value={i.id}>
                  {i.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item className={styles.buttonContainer}>
            <Button type="primary" htmlType="submit" loading={updating} className={styles.submitButton}>
              Save
            </Button>
            <Button onClick={() => setIsEditModalOpen(false)} className={styles.cancelButton}>
              Cancel
            </Button>
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title="Confirm Action"
        open={isConfirmModalOpen}
        onCancel={() => setIsConfirmModalOpen(false)}
        footer={[
          <Button key="cancel" onClick={() => setIsConfirmModalOpen(false)} className={styles.cancelButton}>
            Cancel
          </Button>,
          <Button key="confirm" type="primary" onClick={handleConfirmAction} className={styles.submitButton}>
            Confirm
          </Button>,
        ]}
        className={styles.modal}
      >
        {confirmAction === 'delete' && (
          <p className={styles.modalText}>Are you sure you want to delete {confirmData?.courseName}? This action cannot be undone.</p>
        )}
        {confirmAction === 'unenroll' && (
          <p className={styles.modalText}>
            Are you sure you want to unenroll {confirmData?.student.name} from {confirmData?.courseName}?
          </p>
        )}
        {confirmAction === 'unassign' && (
          <p className={styles.modalText}>
            Are you sure you want to unassign {confirmData?.instructorName} from {confirmData?.courseName}?
          </p>
        )}
        {confirmAction === 'selfUnenroll' && (
          <p className={styles.modalText}>
            Are you sure you want to unenroll from {confirmData?.courseName}?
          </p>
        )}
      </Modal>
      <Modal
        title="Add Student"
        open={isAddStudentModalOpen}
        onCancel={() => setIsAddStudentModalOpen(false)}
        footer={null}
        className={styles.modal}
      >
        <Form form={addStudentForm} onFinish={handleAddStudent} layout="vertical" className={styles.form}>
          <Form.Item
            name="studentId"
            label="Student"
            rules={[{ required: true, message: 'Please select a student' }]}
          >
            <Select placeholder="Select a student" className={styles.select}>
              {students.map((s) => (
                <Option key={s.id} value={s.id}>
                  {s.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item className={styles.buttonContainer}>
            <Button type="primary" htmlType="submit" className={styles.submitButton}>
              Add Student
            </Button>
            <Button onClick={() => setIsAddStudentModalOpen(false)} className={styles.cancelButton}>
              Cancel
            </Button>
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title={userDetails?.name || 'User Details'}
        open={isUserDetailsModalOpen}
        onCancel={() => setIsUserDetailsModalOpen(false)}
        footer={null}
        className={styles.modal}
      >
        {userLoading ? (
          <div className={styles.spinner}>
            <Spin />
          </div>
        ) : userError || !userDetails ? (
          <Alert message={userError || 'User not found'} type="error" showIcon />
        ) : (
          <>
            <Descriptions column={1} bordered className={styles.descriptions}>
              <Descriptions.Item label="Email">{userDetails.email}</Descriptions.Item>
              <Descriptions.Item label="Role">
                <span className={styles.role}>{userDetails.role.toLowerCase()}</span>
              </Descriptions.Item>
            </Descriptions>
            {(userDetails.role === 'STUDENT' || userDetails.role === 'ADMIN') && (
              <>
                <h3 className={styles.sectionTitle}>Enrolled Courses</h3>
                <List
                  dataSource={userEnrolledCourses}
                  renderItem={(userCourse) => (
                    <List.Item>
                      <a
                        onClick={() => {
                          setIsUserDetailsModalOpen(false);
                          // navigate(`/courses/${userCourse.id}`);
                          setTimeout(() => navigate(`/courses/${userCourse.id}`), 500);
                        }}
                        className={styles.courseLink}
                      >
                        {userCourse.name}
                      </a>
                    </List.Item>
                  )}
                  locale={{ emptyText: <span className={styles.noData}>No enrolled courses</span> }}
                  className={styles.courseList}
                />
              </>
            )}
            {(userDetails.role === 'INSTRUCTOR' || userDetails.role === 'ADMIN') && (
              <>
                <h3 className={styles.sectionTitle}>Taught Courses</h3>
                <List
                  dataSource={userTaughtCourses}
                  renderItem={(userCourse) => (
                    <List.Item>
                      <a
                        onClick={() => {
                          setIsUserDetailsModalOpen(false);
                          // navigate(`/courses/${userCourse.id}`);
                          setTimeout(() => navigate(`/courses/${userCourse.id}`), 500);
                        }}
                        className={styles.courseLink}
                      >
                        {userCourse.name}
                      </a>
                    </List.Item>
                  )}
                  locale={{ emptyText: <span className={styles.noData}>No taught courses</span> }}
                  className={styles.courseList}
                />
              </>
            )}
          </>
        )}
      </Modal>
    </div>
  );
}

export default CourseDetails;