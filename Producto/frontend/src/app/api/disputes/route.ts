import { NextResponse } from 'next/server';
import fs from 'fs';
import path from 'path';

// Directorio persistente de almacenamiento dentro del workspace
const STORAGE_DIR = path.join(process.cwd(), 'src', 'data', 'disputes');

// Garantizar que la ruta exista
if (!fs.existsSync(STORAGE_DIR)) {
  fs.mkdirSync(STORAGE_DIR, { recursive: true });
}

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { appId, reason } = body;

    if (!appId) {
      return NextResponse.json({ error: 'Falta el appId de la postulación' }, { status: 400 });
    }

    const disputeData = {
      reason,
      date: new Date().toISOString()
    };

    const filePath = path.join(STORAGE_DIR, `dispute_${appId}.json`);
    fs.writeFileSync(filePath, JSON.stringify(disputeData, null, 2), 'utf-8');

    return NextResponse.json({ success: true, dispute: disputeData });
  } catch (error: any) {
    console.error('Error al guardar disputa en API local:', error);
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const appId = searchParams.get('appId');

    if (!appId) {
      return NextResponse.json({ error: 'Falta el appId' }, { status: 400 });
    }

    const filePath = path.join(STORAGE_DIR, `dispute_${appId}.json`);
    if (fs.existsSync(filePath)) {
      const data = fs.readFileSync(filePath, 'utf-8');
      return NextResponse.json(JSON.parse(data));
    }

    return NextResponse.json(null);
  } catch (error: any) {
    console.error('Error al obtener disputa en API local:', error);
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
