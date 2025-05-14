import { useState, useEffect } from 'react';
import { Form, Input, Button, Modal, Select, message } from 'antd';
import { createCourse, searchUsersByRoleAndName } from '../../services/api';
import { toast } from 'react-toastify';
import styles from '../../styles/CreateCourseModal.module.css';

const { Option } = Select;

function CreateCourseModal({ user, visible, onCancel, onSuccess }) {
  const [form] = Form.useForm();
  const [instructors, setInstructors] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchInstructors = async () => {
    if (user?.role !== 'ADMIN') return;
    try {
      const res = await searchUsersByRoleAndName('INSTRUCTOR', '');
      setInstructors(res.data);
    } catch (error) {
      message.error('Failed to load instructors');
      console.error('Error fetching instructors:', error);
    }
  };

  useEffect(() => {
    if (visible && user?.role === 'ADMIN') {
      fetchInstructors();
    }
    if (visible && user?.role === 'INSTRUCTOR') {
      form.setFieldsValue({ instructorId: user.id });
    }
  }, [visible, user, form]);

  const handleSubmit = async (values) => {
    setLoading(true);
    try {
      const courseData = {
        ...values,
        instructorId: user?.role === 'INSTRUCTOR' ? user.id : values.instructorId,
      };
      await createCourse(courseData);
      toast.success('Course created successfully!');
      form.resetFields();
      onSuccess();
    } catch (error) {
      const errorMessage =
        error.response?.status === 409
          ? 'Course with this name already exists'
          : error.response?.status === 404
          ? 'Instructor not found'
          : 'Failed to create course';
      message.error(errorMessage);
      console.error('Error creating course:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title="Create New Course"
      open={visible}
      onCancel={onCancel}
      footer={null}
      className={styles.modal}
    >
      <Form
        form={form}
        onFinish={handleSubmit}
        layout="vertical"
        initialValues={{ instructorId: user?.role === 'INSTRUCTOR' ? user.id : undefined }}
        className={styles.form}
      >
        <Form.Item
          name="name"
          label="Course Name"
          rules={[
            { required: true, message: 'Course name is required' },
            { max: 255, message: 'Course name must not exceed 255 characters' },
          ]}
        >
          <Input placeholder="Enter course name" className={styles.input} />
        </Form.Item>
        <Form.Item
          name="description"
          label="Description"
          rules={[
            { required: true, message: 'Description is required' },
            { max: 1000, message: 'Description must not exceed 1000 characters' },
          ]}
        >
          <Input.TextArea placeholder="Enter course description" rows={4} className={styles.input} />
        </Form.Item>
        <Form.Item
          name="instructorId"
          label="Instructor"
          rules={[{ required: true, message: 'Instructor is required' }]}
        >
          {user?.role === 'INSTRUCTOR' ? (
            <Input
              disabled
              value={user?.name || 'Instructor Name'}
              className={styles.disabledInput}
            />
          ) : (
            <Select placeholder="Select an instructor" allowClear className={styles.select}>
              {instructors.map((instructor) => (
                <Option key={instructor.id} value={instructor.id}>
                  {instructor.name}
                </Option>
              ))}
            </Select>
          )}
        </Form.Item>
        <Form.Item className={styles.buttonContainer}>
          <Button type="primary" htmlType="submit" loading={loading} className={styles.submitButton}>
            Create Course
          </Button>
          <Button onClick={onCancel} className={styles.cancelButton}>
            Cancel
          </Button>
        </Form.Item>
      </Form>
    </Modal>
  );
}

export default CreateCourseModal;