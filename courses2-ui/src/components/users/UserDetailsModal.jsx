// import { useEffect, useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import {
//   getUserById,
//   getEnrolledCourses,
//   getTaughtCourses,
//   removeStudentFromCourse,
//   unassignInstructorFromCourse,
// } from '../../services/api';
// import { Descriptions, List, Modal, Spin, Alert, Button, message } from 'antd';
// import styles from '../../styles/UserDetails.module.css';

// function UserDetailsModal({ userId, visible, onCancel, currentUser, onChange }) {
//   const [user, setUser] = useState(null);
//   const [enrolledCourses, setEnrolledCourses] = useState([]);
//   const [taughtCourses, setTaughtCourses] = useState([]);
//   const [loading, setLoading] = useState(false);
//   const [error, setError] = useState(null);
//   const navigate = useNavigate();

//   useEffect(() => {
//     if (!visible || !userId) return;

//     const fetchUserData = async () => {
//       try {
//         setLoading(true);
//         const userResponse = await getUserById(userId);
//         const fetchedUser = userResponse.data;
//         if (!fetchedUser || !fetchedUser.id) {
//           throw new Error('Invalid user data');
//         }
//         setUser({
//           id: fetchedUser.id,
//           name: fetchedUser.name || 'Unknown User',
//           email: fetchedUser.email || 'Not provided',
//           role: fetchedUser.role || 'Unknown',
//         });

//         if (fetchedUser.role === 'STUDENT' || fetchedUser.role === 'ADMIN') {
//           const enrolledResponse = await getEnrolledCourses(userId);
//           setEnrolledCourses(enrolledResponse.data || []);
//         } else {
//           setEnrolledCourses([]);
//         }

//         if (fetchedUser.role === 'INSTRUCTOR' || fetchedUser.role === 'ADMIN') {
//           const taughtResponse = await getTaughtCourses(userId);
//           setTaughtCourses(taughtResponse.data || []);
//         } else {
//           setTaughtCourses([]);
//         }

//         setError(null);
//       } catch (err) {
//         setError(err.response?.data?.message || 'Failed to load user data');
//         console.error('Fetch user error:', {
//           message: err.message,
//           status: err.response?.status,
//           data: err.response?.data,
//           url: err.config?.url,
//         });
//         setUser(null);
//       } finally {
//         setLoading(false);
//       }
//     };

//     fetchUserData();
//   }, [userId, visible]);

//   const handleUnenroll = (course) => {
//     console.log('Unenroll clicked:', {
//       courseId: course.id,
//       courseName: course.name,
//       userId,
//       userName: user?.name,
//       instructorId: course.instructor?.id,
//       currentUserId: currentUser?.id,
//     });

//     Modal.confirm({
//       title: `Unenroll ${user?.name || 'Student'}`,
//       content: `Are you sure you want to unenroll ${user?.name || 'the student'} from ${course.name}?`,
//       okText: 'Confirm',
//       cancelText: 'Cancel',
//       onOk: async () => {
//         try {
//           console.log('Calling removeStudentFromCourse:', {
//             courseId: course.id,
//             userId,
//             endpoint: `/courses/${course.id}/student/${userId}`,
//           });
//           await removeStudentFromCourse(course.id, userId);
//           message.success('Student unenrolled successfully');
//           setEnrolledCourses((prev) => prev.filter((c) => c.id !== course.id));
//           if (onChange) {
//             console.log('Calling onChange to refresh CourseDetails');
//             onChange();
//           }
//           console.log('Closing modal');
//           onCancel();
//         } catch (err) {
//           const errorMessage = err.response?.data?.message || 'Failed to unenroll student';
//           message.error(errorMessage);
//           console.error('Unenroll error:', {
//             message: err.message,
//             status: err.response?.status,
//             data: err.response?.data,
//             url: err.config?.url,
//             headers: err.config?.headers,
//           });
//         }
//       },
//     });
//   };

//   const handleUnassign = (course) => {
//     console.log('Unassign clicked:', {
//       courseId: course.id,
//       courseName: course.name,
//       userId,
//       userName: user?.name,
//       instructorId: course.instructor?.id,
//       currentUserId: currentUser?.id,
//     });

//     Modal.confirm({
//       title: `Unassign ${user?.name || 'Instructor'}`,
//       content: `Are you sure you want to unassign ${user?.name || 'the instructor'} from ${course.name}?`,
//       okText: 'Confirm',
//       cancelText: 'Cancel',
//       onOk: async () => {
//         try {
//           console.log('Calling unassignInstructorFromCourse:', {
//             courseId: course.id,
//             userId,
//             endpoint: `/courses/${course.id}/instructor/${userId}`,
//           });
//           await unassignInstructorFromCourse(course.id, userId);
//           message.success('Instructor unassigned successfully');
//           setTaughtCourses((prev) => prev.filter((c) => c.id !== course.id));
//           if (onChange) {
//             console.log('Calling onChange to refresh CourseDetails');
//             onChange();
//           }
//           console.log('Closing modal');
//           onCancel();
//         } catch (err) {
//           const errorMessage = err.response?.data?.message || 'Failed to unassign instructor';
//           message.error(errorMessage);
//           console.error('Unassign error:', {
//             message: err.message,
//             status: err.response?.status,
//             data: err.response?.data,
//             url: err.config?.url,
//             headers: err.config?.headers,
//           });
//         }
//       },
//     });
//   };

//   if (!visible) return null;

//   return (
//     <Modal
//       title={user?.name || 'User Details'}
//       open={visible}
//       onCancel={onCancel}
//       footer={null}
//       className={styles.modal}
//     >
//       {loading ? (
//         <Spin />
//       ) : error || !user ? (
//         <Alert message={error || 'User not found'} type="error" showIcon />
//       ) : (
//         <>
//           <Descriptions column={1} bordered>
//             <Descriptions.Item label="Email">{user.email}</Descriptions.Item>
//             <Descriptions.Item label="Role">
//               <span className="capitalize">{user.role.toLowerCase()}</span>
//             </Descriptions.Item>
//           </Descriptions>
//           {(user.role === 'STUDENT' || user.role === 'ADMIN') && (
//             <>
//               <h3 className="text-lg font-semibold mt-6 mb-4">Enrolled Courses</h3>
//               <List
//                 dataSource={enrolledCourses}
//                 renderItem={(course) => {
//                   const canUnenroll =
//                     currentUser?.role === 'ADMIN' ||
//                     (currentUser?.role === 'INSTRUCTOR' && course.instructor?.id === currentUser?.id);
//                   console.log('Enrolled course:', {
//                     courseId: course.id,
//                     courseName: course.name,
//                     instructorId: course.instructor?.id,
//                     currentUserId: currentUser?.id,
//                     canUnenroll,
//                   });
//                   return (
//                     <List.Item
//                       className={styles.courseItem}
//                       actions={
//                         canUnenroll
//                           ? [
//                               <Button
//                                 type="link"
//                                 danger
//                                 onClick={() => handleUnenroll(course)}
//                                 key="unenroll"
//                                 className={styles.actionButton}
//                               >
//                                 Unenroll
//                               </Button>,
//                             ]
//                           : []
//                       }
//                     >
//                       <a
//                         onClick={() => navigate(`/courses/${course.id}`)}
//                         className="text-blue-600 hover:underline"
//                       >
//                         {course.name}
//                       </a>
//                     </List.Item>
//                   );
//                 }}
//                 locale={{ emptyText: 'No enrolled courses' }}
//               />
//             </>
//           )}
//           {(user.role === 'INSTRUCTOR' || user.role === 'ADMIN') && (
//             <>
//               <h3 className="text-lg font-semibold mt-6 mb-4">Taught Courses</h3>
//               <List
//                 dataSource={taughtCourses}
//                 renderItem={(course) => {
//                   const canUnassign =
//                     currentUser?.role === 'ADMIN' ||
//                     (currentUser?.role === 'INSTRUCTOR' && course.instructor?.id === currentUser?.id);
//                   console.log('Taught course:', {
//                     courseId: course.id,
//                     courseName: course.name,
//                     instructorId: course.instructor?.id,
//                     currentUserId: currentUser?.id,
//                     canUnassign,
//                   });
//                   return (
//                     <List.Item
//                       className={styles.courseItem}
//                       actions={
//                         canUnassign
//                           ? [
//                               <Button
//                                 type="link"
//                                 danger
//                                 onClick={() => handleUnassign(course)}
//                                 key="unassign"
//                                 className={styles.actionButton}
//                               >
//                                 Unassign
//                               </Button>,
//                             ]
//                           : []
//                       }
//                     >
//                       <a
//                         onClick={() => navigate(`/courses/${course.id}`)}
//                         className="text-blue-600 hover:underline"
//                       >
//                         {course.name}
//                       </a>
//                     </List.Item>
//                   );
//                 }}
//                 locale={{ emptyText: 'No taught courses' }}
//               />
//             </>
//           )}
//         </>
//       )}
//     </Modal>
//   );
// }

// export default UserDetailsModal;