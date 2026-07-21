export interface ProductCategory {
  categoryId: number;
  categoryName: string;
  sort: number | null;
}

export interface ProductItem {
  productId: number;
  categoryId: number;
  categoryName: string | null;
  productName: string;
  productImage: string | null;
  productDescription: string | null;
  price: number;
  originalPrice: number | null;
  stock: number;
  sales?: number | null;
  sort?: number | null;
}

export interface ProductListQuery {
  pageNum?: number;
  pageSize?: number;
  categoryId?: number;
  keyword?: string;
}
