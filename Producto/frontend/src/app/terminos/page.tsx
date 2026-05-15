import React from 'react';
import { Card, CardContent } from '@/components/ui/Card';
import { ShieldCheck, Scale, FileText, Lock } from 'lucide-react';

export default function TerminosPage() {
  return (
    <div className="bg-gray-50 min-h-screen py-12 px-4 sm:px-6">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-primary-dark mb-4">Términos y Condiciones</h1>
          <p className="text-gray-500">Última actualización: 10 de Mayo, 2024</p>
        </div>

        <div className="space-y-8">
          <Card className="border-none shadow-sm overflow-hidden">
            <div className="bg-primary p-4 text-white flex items-center gap-3">
              <ShieldCheck size={24} />
              <h2 className="text-xl font-bold">1. Aceptación de los Términos</h2>
            </div>
            <CardContent className="p-8 text-gray-600 leading-relaxed">
              <p>
                Al acceder y utilizar la plataforma **Catch-Go**, usted acepta estar sujeto a estos Términos y Condiciones de Uso. Si no está de acuerdo con alguno de estos términos, le solicitamos que no utilice nuestros servicios. Nuestra plataforma actúa como un intermediario tecnológico para facilitar la conexión entre empresas y trabajadores para turnos ocasionales.
              </p>
            </CardContent>
          </Card>

          <Card className="border-none shadow-sm overflow-hidden">
            <div className="bg-primary p-4 text-white flex items-center gap-3">
              <Scale size={24} />
              <h2 className="text-xl font-bold">2. Responsabilidades del Usuario</h2>
            </div>
            <CardContent className="p-8 text-gray-600 leading-relaxed space-y-4">
              <p>
                Tanto trabajadores como empresas se comprometen a proporcionar información verídica y actualizada. 
              </p>
              <ul className="list-disc pl-5 space-y-2">
                <li>**Trabajadores:** Son responsables de cumplir con los turnos aceptados y mantener una conducta profesional.</li>
                <li>**Empresas:** Son responsables de garantizar condiciones laborales seguras y cumplir con los pagos acordados a través de la plataforma o los medios establecidos.</li>
              </ul>
            </CardContent>
          </Card>

          <Card className="border-none shadow-sm overflow-hidden">
            <div className="bg-primary p-4 text-white flex items-center gap-3">
              <Lock size={24} />
              <h2 className="text-xl font-bold">3. Privacidad y Datos</h2>
            </div>
            <CardContent className="p-8 text-gray-600 leading-relaxed">
              <p>
                La seguridad de sus datos es nuestra prioridad. Catch-Go utiliza servicios de terceros como Supabase para la gestión segura de autenticación y bases de datos. No compartiremos su información personal con terceros fuera del proceso de matching laboral sin su consentimiento explícito.
              </p>
            </CardContent>
          </Card>

          <Card className="border-none shadow-sm overflow-hidden">
            <div className="bg-primary p-4 text-white flex items-center gap-3">
              <FileText size={24} />
              <h2 className="text-xl font-bold">4. Limitación de Responsabilidad</h2>
            </div>
            <CardContent className="p-8 text-gray-600 leading-relaxed">
              <p>
                Catch-Go no es empleador directo. La relación laboral se establece entre la empresa contratante y el trabajador. No nos hacemos responsables por incidentes fuera del control de la plataforma tecnológica durante la ejecución de los turnos.
              </p>
            </CardContent>
          </Card>
        </div>

        <div className="mt-12 text-center text-gray-500 text-sm">
          <p>¿Tienes dudas sobre nuestros términos? Escríbenos a <span className="text-primary font-bold">legal@catchandgo.cl</span></p>
        </div>
      </div>
    </div>
  );
}
