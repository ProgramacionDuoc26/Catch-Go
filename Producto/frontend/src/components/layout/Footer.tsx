import Link from "next/link";

export function Footer() {
  return (
    <footer className="bg-primary-dark border-t border-white/5 py-12 mt-auto">
      <div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row justify-between items-center gap-8 text-center md:text-left">
        <div className="flex flex-col items-center md:items-start">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-2xl font-bold text-white tracking-tighter">CATCH<span className="text-primary-light">AND</span>GO</span>
          </div>
          <p className="text-sm text-white/50 max-w-xs">
            Conectando el talento con las oportunidades en tiempo real. Rápido, seguro y profesional.
          </p>
        </div>
        
        <div className="flex flex-wrap justify-center gap-6 md:gap-8">
          <Link href="/#quienes-somos" className="text-sm text-white/70 hover:text-white transition-colors font-medium">
            ¿Quiénes Somos?
          </Link>
          <Link href="/#contacto" className="text-sm text-white/70 hover:text-white transition-colors font-medium">
            Contacto
          </Link>
          <Link href="/terminos" className="text-sm text-white/70 hover:text-white transition-colors font-medium">
            Términos y Condiciones
          </Link>
        </div>

        <div className="flex flex-col items-center md:items-end gap-2">
          <div className="text-sm text-white/40">
            &copy; {new Date().getFullYear()} Catch&Go.
          </div>
          <p className="text-[10px] text-white/20 uppercase tracking-widest font-bold">Hecho en Chile</p>
        </div>
      </div>
    </footer>
  );
}
