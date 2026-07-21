export type CartSelectedStatus = 0 | 1;
export type ProductStatus = 0 | 1;

export interface AddCartRequest {
  productId: number;
  quantity: number;
}

export interface UpdateCartRequest {
  cartId: number;
  quantity: number;
}

export interface UpdateCartSelectedRequest {
  cartId: number;
  selected: CartSelectedStatus;
}

export interface UpdateCartAllSelectedRequest {
  selected: CartSelectedStatus;
}

export interface CartItem {
  cartId: number;
  storeId: number;
  storeName: string | null;
  productId: number;
  productName: string;
  productImage: string | null;
  price: number | string | null;
  stock: number | null;
  quantity: number;
  selected: CartSelectedStatus;
  productStatus: ProductStatus | null;
  invalidReason: string | null;
  subtotal: number | string | null;
}

export interface CartList {
  storeId: number | null;
  storeName: string | null;
  items: CartItem[];
  selectedTotalAmount: number | string | null;
  selectedProductCount: number;
}
