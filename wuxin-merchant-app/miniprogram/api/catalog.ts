import type { PageResult } from '../types/common';
import type { Category, Product, ProductRequest } from '../types/product';
import { request } from '../utils/request';
export const getCategories = (): Promise<Category[]> => request({ url: '/api/merchant/category/list' });
export const createCategory = (categoryName: string, sort: number): Promise<Category> => request({ url: '/api/merchant/category', method: 'POST', data: { categoryName, sort } });
export const updateCategory = (id: number, categoryName: string, sort: number): Promise<void> => request({ url: `/api/merchant/category/${id}`, method: 'PUT', data: { categoryName, sort } });
export const updateCategoryStatus = (id: number, status: number): Promise<void> => request({ url: `/api/merchant/category/${id}/status`, method: 'PUT', data: { status } });
export const deleteCategory = (id: number): Promise<void> => request({ url: `/api/merchant/category/${id}`, method: 'DELETE' });
export interface ProductQuery { pageNum?: number; pageSize?: number; categoryId?: number; productStatus?: number; keyword?: string; }
function qs(query: ProductQuery): string { return Object.entries(query).filter(([, value]) => value !== undefined && value !== '').map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`).join('&'); }
export const getProducts = (query: ProductQuery): Promise<PageResult<Product>> => request({ url: `/api/merchant/product/list?${qs(query)}` });
export const createProduct = (data: ProductRequest): Promise<Product> => request({ url: '/api/merchant/product', method: 'POST', data });
export const updateProduct = (id: number, data: ProductRequest): Promise<void> => request({ url: `/api/merchant/product/${id}`, method: 'PUT', data });
export const updateProductStatus = (id: number, productStatus: number): Promise<void> => request({ url: `/api/merchant/product/${id}/status`, method: 'PUT', data: { productStatus } });
export const deleteProduct = (id: number): Promise<void> => request({ url: `/api/merchant/product/${id}`, method: 'DELETE' });
