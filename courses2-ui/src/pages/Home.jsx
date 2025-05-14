import { Input, Typography } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
// import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import styles from '../styles/Home.module.css';
import logo from '../assets/logo.png';

const { Title, Paragraph } = Typography;

function Home() {
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
//   const { user } = useContext(AuthContext);

  // Redirect if user is already logged in
//   if (user) {
//     navigate('/courses');
//     return null;
//   }

  const handleSearch = () => {
    if (search.trim()) {
      navigate(`/courses?search=${encodeURIComponent(search.trim())}`);
    }
  };

  return (
    <div className={styles.container}>
      {/* Header-Like Section */}
      <header className={styles.header}>
        <div className={styles.headerContainer}>
          <div className={styles.logo}>
            <a href="/" className={styles.logoLink}>
              <img src={logo} alt="Courses Platform" className={styles.logoIcon} />
              <span>Courses Platform</span>
            </a>
          </div>
          <div className={styles.search}>
            <Input
              name="search"
              id="header-search"
              placeholder="Search courses..."
              prefix={<SearchOutlined className={styles.searchIcon} />}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onPressEnter={handleSearch}
              className={styles.searchInput}
            />
          </div>
          <div className={styles.auth}>
            <div className={styles.authLinks}>
              <a onClick={() => navigate('/login')} className={styles.authLink}>
                Log In
              </a>
              <a onClick={() => navigate('/register')} className={styles.authLink}>
                Register
              </a>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className={styles.hero}>
        <div className={styles.heroContent}>
          <Title level={1} className={styles.heroTitle}>
            Learn Without Limits
          </Title>
          <Paragraph className={styles.heroSubtitle}>
            Advance your career with world-class education. Access thousands of courses from top universities and companies worldwide.
          </Paragraph>
          <div className={styles.heroButtons}>
            <button
              onClick={() => navigate('/register')}
              className={styles.primaryButton}
            >
              Start Learning Today
            </button>
            <button
              onClick={() => navigate('/courses')}
              className={styles.secondaryButton}
            >
              Browse All Courses
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

export default Home;