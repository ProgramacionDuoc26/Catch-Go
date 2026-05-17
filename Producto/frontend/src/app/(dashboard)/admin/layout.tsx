import ProtectedRoute from "@/components/auth/ProtectedRoute";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <ProtectedRoute allowedRoles={["ADMIN"]}>
      <div className="min-h-screen bg-slate-50">
        <div className="py-8 px-4 sm:px-6 lg:px-8">
          {children}
        </div>
      </div>
    </ProtectedRoute>
  );
}
