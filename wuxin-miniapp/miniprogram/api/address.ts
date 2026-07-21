import { request } from '../utils/request';
import type { Address, AddressRequest } from '../types/address';

export function getAddressList(): Promise<Address[]> {
  return request<Address[]>({
    url: '/api/user/address/list'
  });
}

export function createAddress(data: AddressRequest): Promise<string> {
  return request<string, AddressRequest>({
    url: '/api/user/address',
    method: 'POST',
    data
  });
}

export function updateAddress(id: number, data: AddressRequest): Promise<string> {
  return request<string, AddressRequest>({
    url: `/api/user/address/${id}`,
    method: 'PUT',
    data
  });
}

export function setDefaultAddress(id: number): Promise<string> {
  return request<string>({
    url: `/api/user/address/${id}/default`,
    method: 'PUT'
  });
}

export function deleteAddress(id: number): Promise<string> {
  return request<string>({
    url: `/api/user/address/${id}`,
    method: 'DELETE'
  });
}
