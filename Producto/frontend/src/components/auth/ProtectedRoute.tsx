"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { Loader2 } from "lucide-react";

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: ("TRABAJADOR" | "EMPRESA" | "ADMIN")[];
}

export default function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const router = useRouter();
  const pathname = usePathname();
  const [authorized, setAuthorized] = useState(false);

  useEffect(() => {
    const checkAuth = () => {
      const storedUser = localStorage.getItem("user_info");
      
      if (!storedUser) {
        // Redirigir a login si no hay sesión
        router.push(`/login?redirect=${pathname}`);
        return;
      }

      try {
        const user = JSON.parse(storedUser);
        
        // Verificar roles si se especificaron
        if (allowedRoles && !allowedRoles.includes(user.tipo)) {
          // Si el rol no es permitido, redirigir al dashboard correspondiente o al home
          const defaultRedirect = user.tipo === "ADMIN" ? "/admin" : 
                                 user.tipo === "EMPRESA" ? "/empresa/ofertas" : 
                                 "/trabajador/ofertas";
          router.push(defaultRedirect);
          return;
        }

        setAuthorized(true);
      } catch (error) {
        localStorage.removeItem("user_info");
        localStorage.removeItem("auth_token");
        router.push("/login");
      }
    };

    checkAuth();
  }, [router, pathname, allowedRoles]);

  if (!authorized) {
    return (
      <div className="fixed inset-0 bg-white z-[9999] flex flex-col items-center justify-center gap-4">
        <Loader2 className="w-12 h-12 text-primary animate-spin" />
        <p className="text-gray-500 font-medium animate-pulse text-lg">Protegiendo tu sesión...</p>
      </div>
    );
  }

  return <>{children}</>;
}
