import { request } from '@/utils/http'
import type {
  AdminSession, AdminUserItem, BannerItem, BannerPayload, CategoryRow, DashboardData,
  FinanceData, LogQuery, NoticeItem, NoticePayload, OperationLogItem, OrderDetail,
  OrderQuery, OrderRow, PageResult, PermissionItem, ProductQuery, ProductRow,
  RecommendationItem, RecommendationPayload, RiderQuery, RiderRow, RoleItem,
  SystemConfig, UserQuery, UserRow,
} from '@/types/admin'

export const getAdminSession = () => request<AdminSession>({ url: '/api/admin/session' })
export const getDashboard = () => request<DashboardData>({ url: '/api/admin/dashboard' })
export const getOrders = (params: OrderQuery) => request<PageResult<OrderRow>>({ url: '/api/admin/orders', params })
export const getOrderDetail = (id: number) => request<OrderDetail>({ url: `/api/admin/orders/${id}` })
export const cancelOrder = (id: number, reason: string) => request<OrderDetail>({ url: `/api/admin/orders/${id}/cancel`, method: 'POST', data: { reason } })
export const completeOrder = (id: number, reason: string) => request<OrderDetail>({ url: `/api/admin/orders/${id}/complete`, method: 'POST', data: { reason } })
export const getUsers = (params: UserQuery) => request<PageResult<UserRow>>({ url: '/api/admin/users', params })
export const updateUserStatus = (id: number, status: number) => request<UserRow>({ url: `/api/admin/users/${id}/status`, method: 'PUT', data: { status } })
export const getRiders = (params: RiderQuery) => request<PageResult<RiderRow>>({ url: '/api/admin/rider/page', params })
export const getRiderDetail = (id: number) => request<RiderRow>({ url: `/api/admin/rider/${id}` })
export const riderAction = (id: number, action: 'approve' | 'reject' | 'enable' | 'disable', reason?: string) => request({ url: `/api/admin/rider/${id}/${action}`, method: 'POST', data: reason ? { reason } : undefined })
export const getProducts = (params: ProductQuery) => request<PageResult<ProductRow>>({ url: '/api/admin/products', params })
export const getCategories = () => request<CategoryRow[]>({ url: '/api/admin/products/categories' })
export const updateProductStatus = (id: number, status: number) => request<ProductRow>({ url: `/api/admin/products/${id}/status`, method: 'PUT', data: { status } })
export const updateProductFlags = (id: number, recommended: boolean, hot: boolean) => request<ProductRow>({ url: `/api/admin/products/${id}/flags`, method: 'PUT', data: { recommended, hot } })
export const getFinance = () => request<FinanceData>({ url: '/api/admin/finance/summary' })
export const getConfigs = (group?: string) => request<SystemConfig[]>({ url: '/api/admin/configs', params: { group } })
export const updateConfig = (id: number, configValue: string, status: number) => request<SystemConfig>({ url: `/api/admin/configs/${id}`, method: 'PUT', data: { configValue, status } })
export const getBanners = () => request<BannerItem[]>({ url: '/api/admin/operations/banners' })
export const saveBanner = (payload: BannerPayload, id?: number) => request<BannerItem>({ url: id ? `/api/admin/operations/banners/${id}` : '/api/admin/operations/banners', method: id ? 'PUT' : 'POST', data: payload })
export const deleteBanner = (id: number) => request<void>({ url: `/api/admin/operations/banners/${id}`, method: 'DELETE' })
export const getNotices = () => request<NoticeItem[]>({ url: '/api/admin/operations/notices' })
export const saveNotice = (payload: NoticePayload, id?: number) => request<NoticeItem>({ url: id ? `/api/admin/operations/notices/${id}` : '/api/admin/operations/notices', method: id ? 'PUT' : 'POST', data: payload })
export const deleteNotice = (id: number) => request<void>({ url: `/api/admin/operations/notices/${id}`, method: 'DELETE' })
export const getRecommendations = () => request<RecommendationItem[]>({ url: '/api/admin/operations/recommendations' })
export const saveRecommendation = (payload: RecommendationPayload, id?: number) => request<RecommendationItem>({ url: id ? `/api/admin/operations/recommendations/${id}` : '/api/admin/operations/recommendations', method: id ? 'PUT' : 'POST', data: payload })
export const deleteRecommendation = (id: number) => request<void>({ url: `/api/admin/operations/recommendations/${id}`, method: 'DELETE' })
export const getRoles = () => request<RoleItem[]>({ url: '/api/admin/rbac/roles' })
export const getPermissions = () => request<PermissionItem[]>({ url: '/api/admin/rbac/permissions' })
export const getAdminUsers = () => request<AdminUserItem[]>({ url: '/api/admin/rbac/users' })
export const updateAdminUserRoles = (id: number, roleIds: number[]) => request<void>({ url: `/api/admin/rbac/users/${id}/roles`, method: 'PUT', data: { roleIds } })
export const updateRolePermissions = (id: number, permissionIds: number[]) => request<void>({ url: `/api/admin/rbac/roles/${id}/permissions`, method: 'PUT', data: { permissionIds } })
export const getOperationLogs = (params: LogQuery) => request<PageResult<OperationLogItem>>({ url: '/api/admin/logs', params })
