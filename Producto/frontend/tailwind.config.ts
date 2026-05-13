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
          DEFAULT: "#0F52BA", // Azul corporativo (Zafiro)
          dark: "#08367B",
          light: "#3F7CDD",
        },
        background: "#F9FAFB",
        surface: "#FFFFFF",
        text: {
          main: "#111827",
          muted: "#6B7280",
        },
        semantic: {
          success: "#10B981",
          error: "#EF4444",
          warning: "#F59E0B",
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
