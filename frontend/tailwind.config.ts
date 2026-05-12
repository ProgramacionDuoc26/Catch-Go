import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "#0056b3", // Vibrant Blue from logo
          dark: "#001b3a",    // Navy Blue from logo
          light: "#00aaff",
        },
        accent: {
          red: "#e63946",     // Red accent from logo
        },
        background: "#FDFDFD",
        surface: "#FFFFFF",
        text: {
          main: "#001b3a",    // Use Navy Blue for text
          muted: "#64748b",
        },
        semantic: {
          success: "#10B981",
          error: "#e63946",
          warning: "#f59e0b",
        }
      },
      fontFamily: {
        sans: ['var(--font-inter)', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
export default config;
