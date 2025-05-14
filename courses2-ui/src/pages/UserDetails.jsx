// import { useEffect, useState } from 'react';
// import { useParams, useNavigate } from 'react-router-dom';
// import { getUserById, getEnrolledCourses, getTaughtCourses } from '../services/api';
// import { Descriptions, List, Button, Spin, Alert, Card } from 'antd';

// function UserDetails() {
//     const { id } = useParams();
//     const navigate = useNavigate();
//     const [user, setUser] = useState(null);
//     const [enrolledCourses, setEnrolledCourses] = useState([]);
//     const [taughtCourses, setTaughtCourses] = useState([]);
//     const [loading, setLoading] = useState(true);
//     const [error, setError] = useState(null);

//     useEffect(() => {
//         const fetchUserData = async () => {
//             try {
//                 setLoading(true);

//                 // Fetch user details
//                 const userResponse = await getUserById(id);
//                 const fetchedUser = userResponse.data;

//                 // Log response for debugging
//                 console.log('getUserById response:', fetchedUser);

//                 // Validate minimal user data (at least id)
//                 if (!fetchedUser || !fetchedUser.id) {
//                     throw new Error('Invalid user data: missing id');
//                 }

//                 // Set user with fallbacks for missing fields
//                 setUser({
//                     id: fetchedUser.id,
//                     name: fetchedUser.name || 'Unknown User',
//                     email: fetchedUser.email || 'Not provided',
//                     role: fetchedUser.role || 'Unknown'
//                 });

//                 // Fetch courses based on user role
//                 try {
//                     if (fetchedUser.role === 'STUDENT' || fetchedUser.role === 'ADMIN') {
//                         const enrolledResponse = await getEnrolledCourses(id);
//                         setEnrolledCourses(enrolledResponse.data || []);
//                     } else {
//                         setEnrolledCourses([]); // No enrolled courses for instructors
//                     }
//                 } catch (err) {
//                     console.warn('Failed to fetch enrolled courses:', err);
//                     setEnrolledCourses([]); // Set empty list on error
//                 }

//                 try {
//                     if (fetchedUser.role === 'INSTRUCTOR' || fetchedUser.role === 'ADMIN') {
//                         const taughtResponse = await getTaughtCourses(id);
//                         setTaughtCourses(taughtResponse.data || []);
//                     } else {
//                         setTaughtCourses([]); // No taught courses for students
//                     }
//                 } catch (err) {
//                     console.warn('Failed to fetch taught courses:', err);
//                     setTaughtCourses([]); // Set empty list on error
//                 }

//                 setError(null);
//             } catch (err) {
//                 const errorMessage = err.response?.data?.message || 
//                                    err.response?.data?.error || 
//                                    err.message || 
//                                    'Failed to load user data. Please try again.';
//                 setError(errorMessage);
//                 setUser(null); // Ensure user is null on error
//                 console.error('User fetch error:', err);
//             } finally {
//                 setLoading(false);
//             }
//         };

//         fetchUserData();
//     }, [id]);

//     if (loading) {
//         return (
//             <div className="container mx-auto px-4 py-8 flex justify-center">
//                 <Spin size="large" />
//             </div>
//         );
//     }

//     if (error || !user) {
//         return (
//             <div className="container mx-auto px-4 py-8">
//                 <Alert 
//                     message={error || 'User not found'} 
//                     type="error" 
//                     showIcon
//                     action={
//                         <Button 
//                             type="primary" 
//                             onClick={() => navigate('/profile')}
//                         >
//                             Back to Profile
//                         </Button>
//                     }
//                 />
//             </div>
//         );
//     }

//     return (
//         <div className="container mx-auto px-4 py-8">
//             <Card title={user.name} className="shadow-md">
//                 <Descriptions column={1} bordered>
//                     <Descriptions.Item label="Email">{user.email}</Descriptions.Item>
//                     <Descriptions.Item label="Role">
//                         <span className="capitalize">{user.role.toLowerCase()}</span>
//                     </Descriptions.Item>
//                 </Descriptions>

//                 {(user.role === 'STUDENT' || user.role === 'ADMIN') && (
//                     <>
//                         <h3 className="text-lg font-semibold mt-6 mb-4">Enrolled Courses</h3>
//                         <List
//                             dataSource={enrolledCourses}
//                             renderItem={(course) => (
//                                 <List.Item>
//                                     <a
//                                         onClick={() => navigate(`/courses/${course.id}`)}
//                                         className="text-blue-600 hover:underline"
//                                     >
//                                         {course.name}
//                                     </a>
//                                 </List.Item>
//                             )}
//                             locale={{ emptyText: 'No enrolled courses' }}
//                             className="mb-6"
//                         />
//                     </>
//                 )}

//                 {(user.role === 'INSTRUCTOR' || user.role === 'ADMIN') && (
//                     <>
//                         <h3 className="text-lg font-semibold mb-4">Taught Courses</h3>
//                         <List
//                             dataSource={taughtCourses}
//                             renderItem={(course) => (
//                                 <List.Item>
//                                     <a
//                                         onClick={() => navigate(`/courses/${course.id}`)}
//                                         className="text-blue-600 hover:underline"
//                                     >
//                                         {course.name}
//                                     </a>
//                                 </List.Item>
//                             )}
//                             locale={{ emptyText: 'No taught courses' }}
//                         />
//                     </>
//                 )}

//                 <Button
//                     type="primary"
//                     onClick={() => navigate('/profile')}
//                     className="mt-6"
//                 >
//                     Back to Profile
//                 </Button>
//             </Card>
//         </div>
//     );
// }

// export default UserDetails;