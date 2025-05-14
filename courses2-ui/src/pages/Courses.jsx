import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useLocation, useNavigate } from 'react-router-dom';
import { getAllCourses } from '../services/api';
import CourseCard from '../components/courses/CourseCard';
import CreateCourseModal from '../components/courses/CreateCourseModal';
import { Button, Row, Col, message } from 'antd';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import styles from '../styles/Courses.module.css';

function Courses() {
  const { user } = useAuth();
  const [courses, setCourses] = useState([]);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const searchParams = new URLSearchParams(location.search);
  const search = searchParams.get('search');

  const fetchCourses = async () => {
    try {
      const res = await getAllCourses();
      setCourses(
        search
          ? res.data.filter((c) =>
              c.name.toLowerCase().includes(search.toLowerCase())
            )
          : res.data
      );
    } catch (error) {
      message.error('Failed to load courses');
      console.error('Error fetching courses:', error);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, [search]);

  const handleCreateSuccess = () => {
    setIsCreateModalOpen(false);
    fetchCourses();
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Courses</h2>
        {(user?.role === 'ADMIN' || user?.role === 'INSTRUCTOR') && (
          <Button
            type="primary"
            onClick={() => setIsCreateModalOpen(true)}
            className={styles.createButton}
          >
            Create Course
          </Button>
        )}
      </div>
      {courses.length === 0 ? (
        <div className={styles.noCourses}>
          <p className={styles.noCoursesText}>
            {search ? 'No courses match your search' : 'No courses available'}
          </p>
          {search && (
            <Button
              type="primary"
              onClick={() => navigate('/courses')}
              className={styles.exploreButton}
            >
              Explore More Courses
            </Button>
          )}
        </div>
      ) : (
        <Row gutter={[16, 16]} className={styles.courseGrid}>
          {courses.map((course) => (
            <Col key={course.id} xs={24} sm={12} md={8} lg={6}>
              <CourseCard course={course} refreshCourses={fetchCourses} />
            </Col>
          ))}
        </Row>
      )}
      <CreateCourseModal
        user={user}
        visible={isCreateModalOpen}
        onCancel={() => setIsCreateModalOpen(false)}
        onSuccess={handleCreateSuccess}
      />
      <ToastContainer position="top-right" autoClose={3000} />
    </div>
  );
}

export default Courses;