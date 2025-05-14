import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Form, Input, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { loginUser } from '../services/api';
import styles from '../styles/Auth.module.css';

function Login() {
  const { login } = useAuth();
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const onFinish = async (values) => {
    setLoading(true);
    setError(null);
    try {
      const response = await loginUser(values);
      const token = response.data.token;
      login(token);
      message.success('Login successful! Redirecting...');
      setTimeout(() => navigate('/courses'), 1500);
    } catch (err) {
      const errorMessage = err.response?.data?.message ||
                         err.response?.data?.error ||
                         err.message ||
                         'Login failed. Please check your credentials.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h2 className={styles.title}>Login</h2>
        {error && <div className={styles.error}>{error}</div>}
        <Form
          form={form}
          onFinish={onFinish}
          layout="vertical"
          className={styles.form}
        >
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
          <Form.Item>
            <button
              type="submit"
              className={styles.button}
              disabled={loading}
            >
              {loading ? 'Logging in...' : 'Log In'}
            </button>
          </Form.Item>
          <div className={styles.link}>
            Don’t have an account?{' '}
            <a
              onClick={() => navigate('/register')}
              className={styles.linkText}
            >
              Register
            </a>
          </div>
        </Form>
      </div>
    </div>
  );
}

export default Login;