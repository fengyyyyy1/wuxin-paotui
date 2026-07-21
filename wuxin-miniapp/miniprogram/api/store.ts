import { request } from '../utils/request';
import type { PageResult } from '../types/common';
import type { ProductCategory, ProductItem, ProductListQuery } from '../types/product';
import type { StoreDetail, StoreListItem, StoreListQuery } from '../types/store';

export function getStoreList(query: StoreListQuery = {}): Promise<PageResult<StoreListItem>> {
  return request<PageResult<StoreListItem>, StoreListQuery>({
    url: '/api/store/list',
    data: cleanStoreListQuery(query)
  });
}

export function getStoreDetail(id: number): Promise<StoreDetail> {
  return request<StoreDetail>({
    url: `/api/store/${id}`
  });
}

export function getStoreCategories(storeId: number): Promise<ProductCategory[]> {
  return request<ProductCategory[]>({
    url: `/api/store/${storeId}/categories`
  });
}

export function getStoreProducts(
  storeId: number,
  query: ProductListQuery = {}
): Promise<PageResult<ProductItem>> {
  return request<PageResult<ProductItem>, ProductListQuery>({
    url: `/api/store/${storeId}/products`,
    data: cleanProductListQuery(query)
  });
}

export function getProductDetail(productId: number): Promise<ProductItem> {
  return request<ProductItem>({
    url: `/api/store/product/${productId}`
  });
}

function cleanStoreListQuery(query: StoreListQuery): StoreListQuery {
  const params: StoreListQuery = {};
  if (query.pageNum) {
    params.pageNum = query.pageNum;
  }
  if (query.pageSize) {
    params.pageSize = query.pageSize;
  }
  if (query.keyword?.trim()) {
    params.keyword = query.keyword.trim();
  }
  if (query.district?.trim()) {
    params.district = query.district.trim();
  }
  if (query.businessStatus === 0 || query.businessStatus === 1) {
    params.businessStatus = query.businessStatus;
  }
  return params;
}

function cleanProductListQuery(query: ProductListQuery): ProductListQuery {
  const params: ProductListQuery = {};
  if (query.pageNum) {
    params.pageNum = query.pageNum;
  }
  if (query.pageSize) {
    params.pageSize = query.pageSize;
  }
  if (query.categoryId) {
    params.categoryId = query.categoryId;
  }
  if (query.keyword?.trim()) {
    params.keyword = query.keyword.trim();
  }
  return params;
}
