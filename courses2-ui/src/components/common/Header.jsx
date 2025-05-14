import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Input, Avatar, Dropdown } from 'antd';
import { SearchOutlined, UserOutlined, MenuOutlined, CloseOutlined } from '@ant-design/icons';
import styles from '../../styles/Header.module.css';
import logo from '../../assets/logo.png';

function Header() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const handleSearch = () => {
    if (search) {
      navigate(`/courses?search=${encodeURIComponent(search)}`);
      setSearch('');
    }
  };

  const items = [
    {
      key: 'profile',
      label: 'Profile',
      onClick: () => navigate('/profile'),
    },
  ];

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  return (
    <header className={styles.header}>
      <div className={styles.container}>
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
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onPressEnter={handleSearch}
            prefix={<SearchOutlined />}
            className={styles.searchInput}
          />
        </div>
        <div className={styles.auth}>
          {user ? (
            <Dropdown menu={{ items }} trigger={['click']} overlayClassName={styles.dropdown}>
              <a className={styles.avatarLink} onClick={(e) => e.preventDefault()}>
                <Avatar icon={<UserOutlined />} className={styles.avatar} />
              </a>
            </Dropdown>
          ) : (
            <div className={styles.authLinks}>
              <a onClick={() => navigate('/login')} className={styles.authLink}>
                Login
              </a>
              <a onClick={() => navigate('/register')} className={styles.authLink}>
                Register
              </a>
            </div>
          )}
        </div>
        <div className={styles.menuToggle}>
          <a onClick={toggleMenu} className={styles.menuIcon}>
            {isMenuOpen ? <CloseOutlined /> : <MenuOutlined />}
          </a>
        </div>
      </div>
      {isMenuOpen && (
        <div className={styles.mobileMenu}>
          <div className={styles.mobileSearch}>
            <Input
              name="mobile-search"
              id="mobile-search"
              placeholder="Search courses..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onPressEnter={handleSearch}
              prefix={<SearchOutlined />}
              className={styles.searchInput}
            />
          </div>
          {!user && (
            <div className={styles.mobileAuth}>
              <a
                onClick={() => {
                  navigate('/login');
                  setIsMenuOpen(false);
                }}
                className={styles.mobileAuthLink}
              >
                Login
              </a>
              <a
                onClick={() => {
                  navigate('/register');
                  setIsMenuOpen(false);
                }}
                className={styles.mobileAuthLink}
              >
                Register
              </a>
            </div>
          )}
        </div>
      )}
    </header>
  );
}

export default Header;