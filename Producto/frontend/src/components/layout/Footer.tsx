import Link from "next/link";

export function Footer() {
  return (
    <footer className="bg-surface border-t border-gray-200 py-8 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row justify-between items-center gap-4">
        <div className="flex flex-col items-center md:items-start">
          <span className="text-xl font-bold text-primary">Catch&Go</span>
          <p className="text-sm text-text-muted mt-2">
            La plataforma de trabajos ocasionales de Chile.
          </p>
        </div>
        <div className="flex space-x-6">
          <Link href="/quienes-somos" className="text-sm text-text-muted hover:text-primary transition-colors">
            ¿Quiénes Somos?
          </Link>
          <Link href="/contacto" className="text-sm text-text-muted hover:text-primary transition-colors">
            Contacto
          </Link>
          <Link href="/terminos" className="text-sm text-text-muted hover:text-primary transition-colors">
            Términos y Condiciones
          </Link>
        </div>
        <div className="text-sm text-text-muted">
          &copy; {new Date().getFullYear()} Catch&Go. Todos los derechos reservados.
        </div>
      </div>
    </footer>
  );
}
