import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    try {
      // Validate token expiration
      const decoded = jwtDecode(token);
      const isExpired = decoded.exp * 1000 < Date.now();
      if (isExpired) {
        localStorage.removeItem('token');
        throw new Error('Token expired');
      }
      config.headers.Authorization = `Bearer ${token}`;
    } catch (error) {
      console.error('Invalid token:', error);
      localStorage.removeItem('token');
    }
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.log('Server error response:', error.response?.data);
    // Handle 401 Unauthorized
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Authentication Endpoints
export const registerUser = (userData) => api.post('/auth/register', userData);
export const loginUser = (credentials) => api.post('/auth/login', credentials);

// User Endpoints
export const getAllUsers = () => api.get('/users');
export const getCurrentUser = () => api.get('/users/profile');
export const getUserById = (id) => api.get(`/users/${id}`);
export const createUser = (userData) => api.post('/users/create', userData);
export const updateUser = (id, userData) => api.put(`/users/update/${id}`, userData);
export const deleteUser = (id) => api.delete(`/users/delete/${id}`);
export const getUserByEmail = (email) => api.get(`/users/email/${email}`);
export const checkEmailExists = (email) => api.get('/users/check-email', { params: { email } });
export const getCourseStudents = (courseId) => api.get(`/users/course/${courseId}/students`);
export const getCourseInstructors = (courseId) => api.get(`/users/course/${courseId}/instructors`);
export const searchUsersByName = (name) => api.get('/users/search', { params: { name } });
export const searchUsersByRoleAndName = (role, name) =>
  api.get('/users/search-by-role', { params: { role, name } });
export const changeUserRole = (userId, newRole) =>
  api.patch(`/users/${userId}/role`, null, { params: { newRole } });
export const getEnrolledCourses = (userId) => api.get(`/users/${userId}/enrolled-courses`);
export const getTaughtCourses = (userId) => api.get(`/users/${userId}/taught-courses`);

// Course Endpoints
export const getAllCourses = () => api.get('/courses');
export const getCourseById = (id) => api.get(`/courses/${id}`);
export const searchCoursesByName = (name) => api.get('/courses/search', { params: { name } });
export const getCoursesByStudentId = (studentId) => api.get(`/courses/student/${studentId}`);
export const getCoursesByStudentName = (studentName) =>
  api.get('/courses/student/name', { params: { studentName } });
export const getCoursesByInstructorId = (instructorId) =>
  api.get(`/courses/instructor/${instructorId}`);
export const getCoursesByInstructorName = (instructorName) =>
  api.get('/courses/instructor/name', { params: { instructorName } });
export const createCourse = (courseData) => api.post('/courses/create', courseData);
export const createCoursesBulk = (coursesData) => api.post('/courses/bulk', coursesData);
export const updateCourse = (id, courseData) => api.put(`/courses/update/${id}`, courseData);
export const deleteCourse = (id) => api.delete(`/courses/delete/${id}`);
export const addStudentToCourse = (courseId, studentId) =>
  api.post(`/courses/${courseId}/student/${studentId}`);
export const removeStudentFromCourse = (courseId, studentId) =>
  api.delete(`/courses/${courseId}/student/${studentId}`);
export const assignInstructorToCourse = (courseId, instructorId) =>
  api.put(`/courses/${courseId}/instructor/${instructorId}`);
export const unassignInstructorFromCourse = (courseId, instructorId) =>
  api.delete(`/courses/${courseId}/instructor/${instructorId}`);
export const getCoursesByStatus = (status) => api.get(`/courses/status/${status}`);
export const searchCoursesByDescription = (description) =>
  api.get('/courses/search/description', { params: { description } });
export const searchCoursesByStatusAndName = (status, name) =>
  api.get('/courses/search/status', { params: { status, name } });
export const getCoursesWithoutStudents = () => api.get('/courses/without-students');
export const getCoursesWithoutInstructor = () => api.get('/courses/without-instructor');
export const getCoursesWithInstructor = () => api.get('/courses/with-instructor');
export const getCoursesWithStudents = () => api.get('/courses/with-students');

export default api;