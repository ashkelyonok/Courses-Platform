/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", "sans-serif"], // Inspired by wearecheck.co
      },
      colors: {
        primary: "#1E3A8A", // Deep blue from wearecheck.co
        secondary: "#F59E0B", // Warm accent
      },
    },
  },
  plugins: [],
};