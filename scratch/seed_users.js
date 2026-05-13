const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');

// 1. Cargar variables de entorno manualmente
const envPath = path.join(__dirname, '..', 'frontend', '.env.local');
const envContent = fs.readFileSync(envPath, 'utf8');
const env = {};
envContent.split('\n').forEach(line => {
    const match = line.match(/^([^#=]+)=(.+)$/);
    if (match) env[match[1].trim()] = match[2].trim();
});

const SUPABASE_URL = env.NEXT_PUBLIC_SUPABASE_URL;
// Usamos SERVICE_ROLE para crear usuarios confirmados
const SUPABASE_SERVICE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ3dWx5cmdmbGhhbHphbGFraHdvIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3NjU2NTE5MSwiZXhwIjoyMDkyMTQzMTkxfQ.-OBfYlQ1Ha6PTFRpqWfXUfL4RBv8NmQIMxSbHJpB1Tc";
const PROFILE_SERVICE_URL = "http://localhost:8082/profiles";

const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_KEY);

const names = ['Juan', 'Maria', 'Pedro', 'Ana', 'Diego', 'Claudia', 'Roberto', 'Valentina', 'Ignacio', 'Francisca'];
const lastNames = ['Gonzalez', 'Tapia', 'Munoz', 'Rojas', 'Soto', 'Contreras', 'Silva', 'Martinez', 'Sepulveda', 'Morales'];
const companies = ['Construcciones ABC', 'Logística Express', 'Servicios Integrales SpA', 'Retail Chile', 'Tech Solutions', 'Mantenimiento Pro'];

async function seed() {
    console.log('🚀 Iniciando generación de datos de prueba...');

    // Crear 5 Trabajadores
    for (let i = 1; i <= 5; i++) {
        const name = names[Math.floor(Math.random() * names.length)];
        const lastName = lastNames[Math.floor(Math.random() * lastNames.length)];
        const email = `${name.toLowerCase()}${i}@test`;
        
        await createUser(email, `${name} ${lastName}`, 'TRABAJADOR');
    }

    // Crear 5 Empresas
    for (let i = 1; i <= 5; i++) {
        const companyName = companies[Math.floor(Math.random() * companies.length)] + ' ' + i;
        const email = `empresa${i}@test`;
        
        await createUser(email, companyName, 'EMPRESA');
    }

    console.log('✅ Generación completada.');
}

async function createUser(email, name, type) {
    console.log(`Creating ${type}: ${email}...`);
    
    // A. Crear en Supabase Auth
    const { data: authData, error: authError } = await supabase.auth.admin.createUser({
        email: email,
        password: 'Prueba123',
        email_confirm: true,
        user_metadata: { full_name: name }
    });

    if (authError) {
        console.error(`❌ Error creating auth for ${email}:`, authError.message);
        return;
    }

    const userId = authData.user.id;

    // B. Crear en Profile Service
    const profileData = {
        userId: userId,
        name: name,
        email: email,
        phone: '+569' + Math.floor(10000000 + Math.random() * 90000000),
        birthDate: '1990-01-01',
        type: type,
        description: `Perfil de prueba para ${name}`,
        latitude: -33.4489,
        longitude: -70.6693
    };

    try {
        const response = await fetch(PROFILE_SERVICE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(profileData)
        });

        if (!response.ok) {
            const err = await response.text();
            throw new Error(err);
        }

        console.log(`   ✨ Perfil creado para ${email}`);
    } catch (err) {
        console.error(`   ❌ Error creating profile for ${email}:`, err.message);
    }
}

seed();
