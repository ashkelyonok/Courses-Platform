import { useAuth } from '../context/AuthContext';
import { useEffect, useState } from 'react';
import { getCurrentUser, getEnrolledCourses, getTaughtCourses, updateUser } from '../services/api';
import { Descriptions, Card, Button, Modal, Form, Input, Select, message, Spin, Alert, Divider, Empty } from 'antd';
import { EditOutlined, LockOutlined } from '@ant-design/icons';
import CourseCard from '../components/courses/CourseCard';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/Profile.module.css';

const { Option } = Select;

function Profile() {
  const { user: contextUser, logout } = useAuth();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [enrolledCourses, setEnrolledCourses] = useState([]);
  const [taughtCourses, setTaughtCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    const fetchProfileData = async () => {
      try {
        setLoading(true);
        const userResponse = await getCurrentUser();
        const fetchedUser = userResponse.data;
        console.log('getCurrentUser response:', fetchedUser);

        if (!fetchedUser || !fetchedUser.id) {
          throw new Error('Invalid user data: missing id');
        }

        const validatedUser = {
          id: fetchedUser.id,
          name: fetchedUser.name || 'Unknown User',
          email: fetchedUser.email || 'Not provided',
          role: fetchedUser.role || 'Unknown',
        };
        setUser(validatedUser);

        try {
          if (validatedUser.role === 'STUDENT' || validatedUser.role === 'ADMIN') {
            const enrolledResponse = await getEnrolledCourses(validatedUser.id);
            setEnrolledCourses(enrolledResponse.data || []);
          } else {
            setEnrolledCourses([]);
          }
        } catch (err) {
          console.warn('Failed to fetch enrolled courses:', err);
          setEnrolledCourses([]);
        }

        try {
          if (validatedUser.role === 'INSTRUCTOR' || validatedUser.role === 'ADMIN') {
            const taughtResponse = await getTaughtCourses(validatedUser.id);
            setTaughtCourses(taughtResponse.data || []);
          } else {
            setTaughtCourses([]);
          }
        } catch (err) {
          console.warn('Failed to fetch taught courses:', err);
          setTaughtCourses([]);
        }

        setError(null);
      } catch (err) {
        const errorMessage = err.response?.data?.message ||
                            err.response?.data?.error ||
                            err.message ||
                            'Failed to load profile data. Please try again.';
        setError(errorMessage);
        console.error('Profile fetch error:', err);
        if (err.response?.status === 401) {
          message.error('Session expired. Please log in again.');
          logout();
        }
      } finally {
        setLoading(false);
      }
    };

    if (contextUser) fetchProfileData();
  }, [contextUser, logout]);

  const handleEditProfile = () => {
    form.setFieldsValue({
      name: user?.name,
      email: user?.email,
      role: user?.role,
    });
    setIsEditModalOpen(true);
  };

  const handleUpdateProfile = async () => {
    try {
      setUpdating(true);
      const values = await form.validateFields();
      const updateData = {
        name: values.name,
        email: user.email,
        password: values.password || "KEEP_CURRENT_PASSWORD",
        role: values.role,
      };
      const response = await updateUser(user.id, updateData);
      setUser({
        ...user,
        name: response.data.name,
        email: response.data.email,
        role: response.data.role || user.role,
      });
      message.success('Profile updated successfully');
      setIsEditModalOpen(false);
    } catch (err) {
      const errorMessage = err.response?.data?.message ||
                          err.response?.data?.error ||
                          'Failed to update profile. Please try again.';
      message.error(errorMessage);
      console.error('Profile update error:', err);
      if (err.response?.status === 401) {
        message.error('Session expired. Please log in again.');
        logout();
      }
    } finally {
      setUpdating(false);
    }
  };

  const refreshCourses = async () => {
    try {
      if (user.role === 'STUDENT' || user.role === 'ADMIN') {
        const enrolledResponse = await getEnrolledCourses(user.id);
        setEnrolledCourses(enrolledResponse.data || []);
      }
      if (user.role === 'INSTRUCTOR' || user.role === 'ADMIN') {
        const taughtResponse = await getTaughtCourses(user.id);
        setTaughtCourses(taughtResponse.data || []);
      }
    } catch (err) {
      console.warn('Failed to refresh courses:', err);
      message.error('Failed to refresh courses. Please try again.');
    }
  };

  if (!contextUser) {
    return (
      <div className={styles.container}>
        <Alert
          message="You must be registered to access your profile"
          type="warning"
          showIcon
          action={
            <Button
              type="primary"
              onClick={() => navigate('/register')}
              className={styles.actionButton}
            >
              Register
            </Button>
          }
        />
      </div>
    );
  }

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.spinner}>
          <Spin size="large" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.container}>
        <Alert
          message={error}
          type="error"
          showIcon
          action={
            <Button
              type="primary"
              onClick={() => window.location.reload()}
              className={styles.actionButton}
            >
              Try Again
            </Button>
          }
        />
      </div>
    );
  }

  const hasEnrolledCourses = enrolledCourses.length > 0;
  const hasTaughtCourses = taughtCourses.length > 0;
  const hasCourses = hasEnrolledCourses || hasTaughtCourses;

  return (
    <div className={styles.container}>
      <Card
        title="User Profile"
        className={styles.profileCard}
        extra={
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={handleEditProfile}
            className={styles.editButton}
          />
        }
      >
        <Descriptions bordered column={1} className={styles.descriptions}>
          <Descriptions.Item label="Name">
            <span className={styles.profileName}>{user.name}</span>
          </Descriptions.Item>
          <Descriptions.Item label="Email">{user.email}</Descriptions.Item>
          <Descriptions.Item label="Role">
            <span className={styles.role}>{user.role.toLowerCase()}</span>
          </Descriptions.Item>
        </Descriptions>

        <Divider orientation='left' orientationMargin={0} className={styles.divider}>
          <span className={styles.sectionTitle}>My Courses</span>
        </Divider>

        {!hasCourses ? (
          <div className={styles.noCourses}>
            <Empty
              description={
                <span className={styles.noCoursesText}>
                  No courses available
                </span>
              }
            >
              <Button
                type="primary"
                onClick={() => navigate('/courses')}
                className={styles.exploreButton}
              >
                Explore Courses
              </Button>
            </Empty>
          </div>
        ) : (
          <div className={styles.courseGrid}>
            {user.role === 'ADMIN' && (
              <>
                <Divider orientation="left" className={styles.divider}>
                  <span className={styles.subSectionTitle}>Enrolled Courses</span>
                </Divider>
                {hasEnrolledCourses ? (
                  enrolledCourses.map((course) => (
                    <CourseCard
                      key={`enrolled-${course.id}`}
                      course={course}
                      refreshCourses={refreshCourses}
                    />
                  ))
                ) : (
                  <div className={styles.noCourses}>
                    <Empty
                      description={
                        <span className={styles.noCoursesText}>
                          No enrolled courses
                        </span>
                      }
                    >
                      <Button
                        type="primary"
                        onClick={() => navigate('/courses')}
                        className={styles.exploreButton}
                      >
                        Explore Courses
                      </Button>
                    </Empty>
                  </div>
                )}

                <Divider orientation="left" className={styles.divider}>
                  <span className={styles.subSectionTitle}>Taught Courses</span>
                </Divider>
                {hasTaughtCourses ? (
                  taughtCourses.map((course) => (
                    <CourseCard
                      key={`taught-${course.id}`}
                      course={course}
                      refreshCourses={refreshCourses}
                    />
                  ))
                ) : (
                  <div className={styles.noCourses}>
                    <Empty
                      description={
                        <span className={styles.noCoursesText}>
                          No taught courses
                        </span>
                      }
                    />
                  </div>
                )}
              </>
            )}

            {user.role === 'STUDENT' && (
              <>
                {hasEnrolledCourses ? (
                  enrolledCourses.map((course) => (
                    <CourseCard
                      key={`enrolled-${course.id}`}
                      course={course}
                      refreshCourses={refreshCourses}
                    />
                  ))
                ) : (
                  <div className={styles.noCourses}>
                    <Empty
                      description={
                        <span className={styles.noCoursesText}>
                          No enrolled courses
                        </span>
                      }
                    >
                      <Button
                        type="primary"
                        onClick={() => navigate('/courses')}
                        className={styles.exploreButton}
                      >
                        Explore Courses
                      </Button>
                    </Empty>
                  </div>
                )}
              </>
            )}

            {user.role === 'INSTRUCTOR' && (
              <>
                {hasTaughtCourses ? (
                  taughtCourses.map((course) => (
                    <CourseCard
                      key={`taught-${course.id}`}
                      course={course}
                      refreshCourses={refreshCourses}
                    />
                  ))
                ) : (
                  <div className={styles.noCourses}>
                    <Empty
                      description={
                        <span className={styles.noCoursesText}>
                          No taught courses
                        </span>
                      }
                    />
                  </div>
                )}
              </>
            )}
          </div>
        )}

        <div className={styles.logoutSection}>
          <Button
            onClick={() => {
              logout();
              navigate('/');
            }}
            className={styles.logoutButton}
          >
            Logout
          </Button>
        </div>
      </Card>

      <Modal
        title="Edit Profile"
        open={isEditModalOpen}
        onCancel={() => setIsEditModalOpen(false)}
        footer={null}
        className={styles.modal}
      >
        <Form
          form={form}
          onFinish={handleUpdateProfile}
          layout="vertical"
          initialValues={{
            role: user?.role,
          }}
          className={styles.form}
        >
          <Form.Item
            name="name"
            label="Full Name"
            rules={[
              { required: true, message: 'Please input your name!' },
              { max: 127, message: 'Name must not exceed 127 characters' },
            ]}
          >
            <Input placeholder="Enter your full name" className={styles.input} />
          </Form.Item>

          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: 'Please input your email!' },
              { type: 'email', message: 'Please enter a valid email' },
            ]}
          >
            <Input disabled placeholder="Your email (cannot be changed)" className={styles.input} />
          </Form.Item>

          <Form.Item
            name="password"
            label="New Password (optional)"
            rules={[
              { min: 5, message: 'Password must be at least 5 characters' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="Leave blank to keep current password"
              className={styles.input}
            />
          </Form.Item>

          {contextUser?.role === 'ADMIN' && (
            <Form.Item
              name="role"
              label="Role"
              rules={[{ required: true, message: 'Please select a role!' }]}
            >
              <Select className={styles.select}>
                <Option value="STUDENT">Student</Option>
                <Option value="INSTRUCTOR">Instructor</Option>
                <Option value="ADMIN">Admin</Option>
              </Select>
            </Form.Item>
          )}

          <Form.Item className={styles.buttonContainer}>
            <Button
              type="primary"
              htmlType="submit"
              loading={updating}
              className={styles.submitButton}
            >
              Save
            </Button>
            <Button onClick={() => setIsEditModalOpen(false)} className={styles.cancelButton}>
              Cancel
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default Profile;