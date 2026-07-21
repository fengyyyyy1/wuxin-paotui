import type {
  AddCartRequest,
  CartItem,
  CartList,
  UpdateCartAllSelectedRequest,
  UpdateCartRequest,
  UpdateCartSelectedRequest
} from '../types/cart';
import { request } from '../utils/request';

export function addCart(data: AddCartRequest): Promise<CartItem> {
  return request<CartItem, AddCartRequest>({
    url: '/api/cart/add',
    method: 'POST',
    data
  });
}

export function getCartList(): Promise<CartList> {
  return request<CartList>({
    url: '/api/cart/list'
  });
}

export function updateCartQuantity(data: UpdateCartRequest): Promise<CartItem> {
  return request<CartItem, UpdateCartRequest>({
    url: '/api/cart/update',
    method: 'PUT',
    data
  });
}

export function updateCartSelected(data: UpdateCartSelectedRequest): Promise<CartItem> {
  return request<CartItem, UpdateCartSelectedRequest>({
    url: '/api/cart/selected',
    method: 'PUT',
    data
  });
}

export function updateCartAllSelected(data: UpdateCartAllSelectedRequest): Promise<CartList> {
  return request<CartList, UpdateCartAllSelectedRequest>({
    url: '/api/cart/selected/all',
    method: 'PUT',
    data
  });
}

export function deleteCartItem(cartId: number): Promise<void> {
  return request<void>({
    url: `/api/cart/${cartId}`,
    method: 'DELETE'
  });
}

export function clearInvalidCart(): Promise<void> {
  return request<void>({
    url: '/api/cart/invalid',
    method: 'DELETE'
  });
}

export function clearCart(): Promise<void> {
  return request<void>({
    url: '/api/cart/clear',
    method: 'DELETE'
  });
}
