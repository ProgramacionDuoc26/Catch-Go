import Link from "next/link";
import { User, LogIn } from "lucide-react";

export function Navbar() {
  return (
    <header className="bg-surface border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link href="/" className="flex items-center gap-2">
              <span className="text-2xl font-bold text-primary">Catch&Go</span>
            </Link>
          </div>
          <nav className="hidden md:flex space-x-8">
            <Link href="/quienes-somos" className="text-text-muted hover:text-primary transition-colors">
              ¿Quiénes Somos?
            </Link>
            <Link href="/contacto" className="text-text-muted hover:text-primary transition-colors">
              Contacto
            </Link>
          </nav>
          <div className="flex items-center space-x-4">
            <Link
              href="/login"
              className="hidden md:flex items-center gap-2 text-text-main hover:text-primary font-medium"
            >
              <LogIn className="w-5 h-5" />
              Ingresar
            </Link>
            <Link
              href="/register"
              className="bg-primary text-white px-4 py-2 rounded-md hover:bg-primary-dark transition-colors font-semibold flex items-center gap-2 min-h-[44px]"
            >
              <User className="w-5 h-5" />
              Regístrate
            </Link>
          </div>
        </div>
      </div>
    </header>
  );
}
