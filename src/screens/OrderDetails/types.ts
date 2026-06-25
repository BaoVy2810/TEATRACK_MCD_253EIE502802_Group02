export type OrderStatus =
  | 'pending'
  | 'processing'
  | 'ready'
  | 'shipping'
  | 'completed'
  | 'cancelled';

export interface OrderItem {
  id: string;
  productId?: string;
  productName: string;
  image?: string;
  size: 'M' | 'L';
  sugar: string;
  ice: string;
  toppings?: string[];
  quantity: number;
  unitPrice: number; // VND
}

export interface Order {
  id: string;
  orderId: string;        // e.g. "HTNG-52422"
  createdAt: string;      // ISO 8601
  paymentMethod: string;  // "MoMo" | "Cash" | "VNPay" | …
  status: OrderStatus;
  customerName: string;
  phoneNumber: string;
  deliveryAddress: string;
  branchAddress?: string;
  items: OrderItem[];
  subtotal: number;
  discount: number;
  shippingFee: number;
  total: number;
}
