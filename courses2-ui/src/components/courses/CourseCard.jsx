import { Card, Button, Modal, message } from 'antd';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import {
    deleteCourse,
    addStudentToCourse,
    removeStudentFromCourse,
} from '../../services/api';
import { useState } from 'react';
import styles from '../../styles/CourseCard.module.css';

function CourseCard({ course, refreshCourses }) {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalAction, setModalAction] = useState('');

    const handleDelete = async () => {
        try {
            await deleteCourse(course.id);
            message.success('Course deleted successfully');
            refreshCourses();
            setIsModalOpen(false);
        } catch (err) {
            message.error('Failed to delete course. Please try again.');
            console.error('Delete course error:', err);
            setIsModalOpen(false);
        }
    };

    const handleEnroll = async () => {
        try {
            await addStudentToCourse(course.id, user.id);
            message.success('Enrolled in course successfully');
            refreshCourses();
        } catch (err) {
            message.error('Failed to enroll in course. Please try again.');
            console.error('Enroll error:', err);
        }
    };

    const handleUnenroll = async () => {
        try {
            await removeStudentFromCourse(course.id, user.id);
            message.success('Unenrolled from course successfully');
            refreshCourses();
            setIsModalOpen(false);
        } catch (err) {
            message.error('Failed to unenroll from course. Please try again.');
            console.error('Unenroll error:', err);
            setIsModalOpen(false);
        }
    };

    const openModal = (action) => {
        setModalAction(action);
        setIsModalOpen(true);
    };

    const confirmAction = async () => {
        if (modalAction === 'delete') {
            await handleDelete();
        } else if (modalAction === 'unenroll') {
            await handleUnenroll();
        }
    };

    const isEnrolled = course.students.some((s) => s.id === user?.id);
    const isInstructor = user?.role === 'INSTRUCTOR' && course.instructor?.id === user.id;
    const isAdmin = user?.role === 'ADMIN';

    return (
        <>
            <Card
                title={course.name}
                className={styles.card}
                onClick={() => navigate(`/courses/${course.id}`)}
            >
                <p className={styles.description}>{course.description}</p>
                <div className={styles.buttonContainer}>
                    {(isAdmin || isInstructor) && (
                        <Button
                            danger
                            onClick={(e) => {
                                e.stopPropagation();
                                openModal('delete');
                            }}
                            className={styles.button}
                        >
                            Delete
                        </Button>
                    )}
                    {user?.role === 'STUDENT' && !isEnrolled && (
                        <Button
                            type="primary"
                            onClick={(e) => {
                                e.stopPropagation();
                                handleEnroll();
                            }}
                            className={styles.button}
                        >
                            Enroll
                        </Button>
                    )}
                    {user?.role === 'STUDENT' && isEnrolled && (
                        <Button
                            onClick={(e) => {
                                e.stopPropagation();
                                openModal('unenroll');
                            }}
                            className={styles.button}
                        >
                            Unenroll
                        </Button>
                    )}
                </div>
            </Card>
            <Modal
                title={modalAction === 'delete' ? 'Confirm Deletion' : 'Confirm Unenroll'}
                open={isModalOpen}
                onOk={confirmAction}
                onCancel={() => setIsModalOpen(false)}
                okText="Confirm"
                cancelText="Cancel"
                className={styles.modal}
            >
                <p className={styles.modalText}>
                    {modalAction === 'delete'
                        ? 'Are you sure you want to delete this course? This action cannot be undone.'
                        : 'Are you sure you want to unenroll from this course?'}
                </p>
            </Modal>
        </>
    );
}

export default CourseCard;