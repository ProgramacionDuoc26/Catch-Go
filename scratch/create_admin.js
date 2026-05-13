const fs = require('fs');
const path = require('path');

// URLs de los microservicios locales
const AUTH_SERVICE_URL = "http://localhost:8081/auth";
const PROFILE_SERVICE_URL = "http://localhost:8082/profiles";

async function createAdmin() {
    const adminData = {
        email: 'miguel@admin.com',
        password: 'Admin1234',
        nombre: 'Miguel Admin',
        tipo: 'ADMIN',
        telefono: '+56900000000'
    };

    console.log(`🚀 Creando Usuario Administrador Local: ${adminData.email}...`);
    
    try {
        // 1. Registrar en Auth Service
        console.log('   Enviando registro a Auth Service (8081)...');
        const authResponse = await fetch(`${AUTH_SERVICE_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(adminData)
        });

        if (!authResponse.ok) {
            const errText = await authResponse.text();
            if (errText.includes("ya está registrado")) {
                console.log('   ℹ️ El correo ya está registrado en Auth Service.');
                // Continuamos para asegurar que el perfil exista
            } else {
                throw new Error(`Auth Error: ${errText}`);
            }
        } else {
            console.log('   ✅ Usuario registrado en Auth Service.');
        }

        // 2. Obtener el ID del usuario (necesitamos loguearnos o que el registro nos lo de)
        // El registro devuelve AuthResponseDto con el usuario
        const authResult = authResponse.ok ? await authResponse.json() : null;
        let userId;

        if (authResult && authResult.usuario) {
            userId = authResult.usuario.id;
        } else {
            // Si ya existía, intentamos login para obtener el ID
            console.log('   Intentando login para recuperar ID...');
            const loginRes = await fetch(`${AUTH_SERVICE_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: adminData.email, password: adminData.password })
            });
            if (loginRes.ok) {
                const loginData = await loginRes.json();
                userId = loginData.usuario.id;
            } else {
                throw new Error('No se pudo obtener el ID del usuario.');
            }
        }

        // 3. Crear Perfil en Profile Service
        console.log(`   Creando perfil para ID: ${userId}...`);
        const profileData = {
            userId: userId.toString(),
            name: adminData.nombre,
            email: adminData.email,
            phone: adminData.telefono,
            birthDate: '1985-01-01',
            type: 'ADMIN',
            description: 'Administrador del Sistema',
            latitude: -33.4489,
            longitude: -70.6693
        };

        const profileRes = await fetch(PROFILE_SERVICE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(profileData)
        });

        if (!profileRes.ok) {
            const err = await profileRes.text();
            throw new Error(`Profile Error: ${err}`);
        }

        console.log('✅ Administrador creado con éxito en los microservicios locales.');
        console.log(`   Email: ${adminData.email}`);
        console.log(`   Pass:  ${adminData.password}`);

    } catch (err) {
        console.error(`❌ Error crítico:`, err.message);
        console.log('\n💡 Asegúrate de que los microservicios (Docker) estén corriendo:');
        console.log('   - Auth Service: 8081');
        console.log('   - Profile Service: 8082');
    }
}

createAdmin();
