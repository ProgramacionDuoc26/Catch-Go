export default function QuienesSomosPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-16">
      <h1 className="text-4xl font-bold text-text-main mb-8 text-center">¿Quiénes Somos?</h1>
      <div className="bg-surface rounded-xl shadow-sm border border-gray-100 p-8">
        <p className="text-lg text-text-muted mb-6">
          En <span className="font-semibold text-primary">Catch&Go</span>, entendemos que el mundo laboral es dinámico. Nuestra misión es conectar de manera rápida, segura y transparente a empresas que necesitan cubrir urgencias operativas con trabajadores dispuestos a tomar turnos ocasionales.
        </p>
        <h2 className="text-2xl font-bold text-text-main mb-4">Nuestra Visión</h2>
        <p className="text-lg text-text-muted mb-6">
          Ser la principal plataforma de matching laboral para trabajos temporales en Chile (Región Metropolitana, V y VI región), reduciendo la fricción al mínimo para que la contratación ocasional sea tan sencilla como un clic.
        </p>
        <h2 className="text-2xl font-bold text-text-main mb-4">Nuestros Valores</h2>
        <ul className="list-disc list-inside text-lg text-text-muted space-y-2">
          <li>Transparencia en las ofertas y remuneraciones.</li>
          <li>Agilidad para resolver urgencias.</li>
          <li>Confianza y verificación de perfiles.</li>
          <li>Accesibilidad para todos los usuarios.</li>
        </ul>
      </div>
    </div>
  );
}
