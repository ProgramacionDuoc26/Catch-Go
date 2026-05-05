import Link from "next/link";

export default function Home() {
  return (
    <div className="flex flex-col items-center justify-center flex-grow py-20 px-4 text-center">
      <h1 className="text-4xl md:text-6xl font-bold text-text-main mb-6">
        Encuentra el <span className="text-primary">Talento Temporal</span> que Necesitas
      </h1>
      <p className="text-lg md:text-xl text-text-muted max-w-2xl mb-10">
        Conectamos empresas con trabajadores verificados para turnos ocasionales. Rápido, seguro y sin fricción.
      </p>
      
      <div className="flex flex-col sm:flex-row gap-4 w-full justify-center max-w-md">
        <Link 
          href="/register?type=empresa" 
          className="flex items-center justify-center px-8 py-4 bg-primary text-white font-semibold rounded-lg shadow-md hover:bg-primary-dark transition-colors min-h-[48px]"
        >
          Busco Trabajadores
        </Link>
        <Link 
          href="/register?type=trabajador" 
          className="flex items-center justify-center px-8 py-4 bg-surface text-primary border border-primary font-semibold rounded-lg hover:bg-gray-50 transition-colors min-h-[48px]"
        >
          Busco Empleo
        </Link>
      </div>
    </div>
  );
}
