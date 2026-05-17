import { api } from './client';

const BASE = (process.env.NEXT_PUBLIC_PROFILE_SERVICE_URL || 'http://localhost:8082') + '/profiles/webpay';

export interface WebpayInitResponse {
  token: string;
  url: string;
  buyOrder: string;
}

export interface WebpayConfirmResponse {
  status: 'AUTHORIZED' | 'FAILED';
  response_code?: number;
  amount?: number;
  buy_order?: string;
  session_id?: string;
  accounting_date?: string;
  transaction_date?: string;
  payment_type_code?: string;
  installments_number?: number;
}

export const paymentApi = {
  /**
   * Inicializar pago de Webpay Plus
   */
  initWebpay: (userId: string, amount: number, returnUrl: string) =>
    api.post<WebpayInitResponse>(`${BASE}/init`, { userId, amount, returnUrl }),

  /**
   * Confirmar pago de Webpay Plus
   */
  confirmWebpay: (token: string) =>
    api.post<WebpayConfirmResponse>(`${BASE}/confirm`, { token }),
};
