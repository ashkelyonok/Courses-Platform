import React from 'react';
import { TwitterOutlined, LinkedinOutlined } from '@ant-design/icons';
import styles from '../../styles/Footer.module.css';
import logo from '../../assets/logo-white.png';

const Footer = () => {
  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        <div className={styles.logo}>
          <img src={logo} alt="Courses Platform" className={styles.logoIcon} />
          <span>Courses Platform</span>
        </div>
        <div className={styles.links}>
          <div className={styles.linkColumn}>
            <h4 className={styles.linkTitle}>Platform</h4>
            <a href="/courses" className={styles.link}>Courses</a>
            <a href="/about" className={styles.link}>About</a>
            <a href="/contact" className={styles.link}>Contact</a>
          </div>
          <div className={styles.linkColumn}>
            <h4 className={styles.linkTitle}>Legal</h4>
            <a href="/privacy" className={styles.link}>Privacy Policy</a>
            <a href="/terms" className={styles.link}>Terms of Service</a>
          </div>
        </div>
        <div className={styles.social}>
          <a href="https://twitter.com" target="_blank" rel="noopener noreferrer" className={styles.socialLink}>
            <TwitterOutlined className={styles.socialIcon} />
          </a>
          <a href="https://linkedin.com" target="_blank" rel="noopener noreferrer" className={styles.socialLink}>
            <LinkedinOutlined className={styles.socialIcon} />
          </a>
        </div>
        <div className={styles.copyright}>
          © 2025 Courses Platform. All rights reserved.
        </div>
      </div>
    </footer>
  );
};

export default Footer;