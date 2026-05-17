import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Navbar } from "@/components/layout/Navbar";
import { Footer } from "@/components/layout/Footer";
import { NotificationProvider } from "@/context/NotificationContext";
import { SettingsProvider } from "@/context/SettingsContext";
import { Toaster } from "sonner";

const inter = Inter({ subsets: ["latin"], variable: "--font-inter" });

export const metadata: Metadata = {
  title: "Catch & Go - Trabajos Ocasionales",
  description: "Plataforma de matching entre empresas y trabajadores para trabajos ocasionales en Chile.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="es">
      <body className={`${inter.variable} min-h-screen bg-background font-sans flex flex-col`}>
        <SettingsProvider>
          <NotificationProvider>
            <Toaster position="top-right" expand={false} richColors />
            <Navbar />
            <main className="flex-grow flex flex-col">
              {children}
            </main>
            <Footer />
          </NotificationProvider>
        </SettingsProvider>
      </body>
    </html>
  );
}
