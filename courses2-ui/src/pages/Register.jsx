import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Form, Input, Select, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { registerUser } from '../services/api';
import styles from '../styles/Auth.module.css';

const { Option } = Select;

function Register() {
  const { login } = useAuth();
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const onFinish = async (values) => {
    setLoading(true);
    setError(null);
    try {
      const registrationData = {
        name: values.name,
        email: values.email,
        password: values.password,
        role: values.role,
      };
      const response = await registerUser(registrationData);
      const token = response.data.token;
      login(token);
      message.success('Registration successful! Redirecting...');
      setTimeout(() => navigate('/courses'), 1500);
    } catch (err) {
      const errorMessage = err.response?.data?.message ||
                         err.response?.data?.error ||
                         err.message ||
                         'Registration failed. Please try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h2 className={styles.title}>Register</h2>
        {error && <div className={styles.error}>{error}</div>}
        <Form
          form={form}
          onFinish={onFinish}
          layout="vertical"
          className={styles.form}
        >
          <Form.Item
            name="name"
            label="Name"
            rules={[
              { required: true, message: 'Please input your name!' },
              { max: 127, message: 'Name must be less than 127 characters' },
            ]}
          >
            <Input
              placeholder="Enter your full name"
              className={styles.input}
            />
          </Form.Item>
          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: 'Please input your email!' },
              { type: 'email', message: 'Invalid email format' },
            ]}
          >
            <Input
              placeholder="Enter your email"
              className={styles.input}
            />
          </Form.Item>
          <Form.Item
            name="password"
            label="Password"
            rules={[
              { required: true, message: 'Please input your password!' },
              { min: 5, message: 'Password must be at least 5 characters' },
            ]}
          >
            <Input.Password
              placeholder="Enter your password"
              className={styles.input}
            />
          </Form.Item>
          <Form.Item
            name="role"
            label="Role"
            rules={[{ required: true, message: 'Please select a role!' }]}
          >
            <Select
              placeholder="Select a role"
              className={styles.input}
            >
              <Option value="STUDENT">Student</Option>
              <Option value="INSTRUCTOR">Instructor</Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <button
              type="submit"
              className={styles.button}
              disabled={loading}
            >
              {loading ? 'Registering...' : 'Register'}
            </button>
          </Form.Item>
          <div className={styles.link}>
            Already have an account?{' '}
            <a
              onClick={() => navigate('/login')}
              className={styles.linkText}
            >
              Log In
            </a>
          </div>
        </Form>
      </div>
    </div>
  );
}

export default Register;