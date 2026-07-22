export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface AdminSession {
  userId: number
  username: string
  nickname: string | null
  roles: string[]
  permissions: string[]
}

export interface TrendPoint { date: string; value: number }
export interface RankingItem { id: number; name: string; value: number; count: number }
export interface OrderRow {
  orderId: number; orderNo: string; orderType: number; orderTypeText: string
  userId: number; userName: string; riderId: number | null; riderName: string | null
  merchantId: number | null; storeId: number | null; storeName: string | null
  goodsName: string; totalAmount: number; payStatus: number; status: number
  statusText: string; createTime: string; finishTime: string | null; abnormal: boolean
}
export interface OrderItem { productId: number; productName: string; productImage: string; productPrice: number; quantity: number; subtotal: number }
export interface OrderLog { id: number; oldStatus: number; newStatus: number; operatorId: number; operatorType: string; remark: string; createTime: string }
export interface OrderDetail extends OrderRow {
  productAmount: number; deliveryFee: number; weight: number | null; distance: number | null
  remark: string | null; payTime: string | null; items: OrderItem[]; logs: OrderLog[]
}
export interface DashboardData {
  todayOrders: number; todayRevenue: number; todayDeliveries: number; newUsers: number
  newRiders: number; newMerchants: number; pendingRiders: number; pendingMerchants: number
  pendingRefunds: number; orderTrend: TrendPoint[]; revenueTrend: TrendPoint[]
  topProducts: RankingItem[]; topMerchants: RankingItem[]; topRiders: RankingItem[]
  recentOrders: OrderRow[]; notices: NoticeItem[]
}
export interface UserRow {
  userId: number; username: string; nickname: string | null; avatar: string | null; phone: string | null
  status: number; orderCount: number; consumptionAmount: number; lastLoginTime: string | null; createTime: string
}
export interface RiderRow {
  riderId: number; userId: number; username: string; nickname: string | null; phone: string | null
  realName: string; idCard: string; idCardFront: string; idCardBack: string
  auditStatus: number; auditStatusText: string; riderStatus: number; riderStatusText: string
  rejectReason: string | null; deliveryCount: number; completedCount: number
  completionRate: number; createTime: string; updateTime: string
}
export interface ProductRow {
  productId: number; storeId: number; storeName: string; merchantId: number; categoryId: number
  categoryName: string; productName: string; productImage: string; price: number; stock: number
  sales: number; productStatus: number; recommended: boolean; hot: boolean; updateTime: string
}
export interface CategoryRow { categoryId: number; storeId: number; storeName: string; categoryName: string; status: number; sort: number; productCount: number }
export interface FinanceData {
  platformRevenue: number; todayIncome: number; yesterdayIncome: number; monthIncome: number
  orderAmount: number; platformCommission: number; merchantIncome: number; riderIncome: number
  platformCommissionRate: number; merchantCommissionRate: number; riderRewardRate: number
}
export interface SystemConfig {
  id: number; configGroup: string; configKey: string; configValue: string; valueType: string
  configName: string; configDescription: string | null; sensitive: number; status: number
  updateAdminId: number | null; updateTime: string
}
export interface BannerItem {
  id: number; title: string; subtitle: string | null; imageUrl: string; targetType: string
  targetValue: string | null; sort: number; status: number; startTime: string | null
  endTime: string | null; updateTime: string
}
export interface NoticeItem {
  id: number; noticeType: string; title: string; content: string; status: number
  publishTime: string | null; expireTime: string | null; updateTime: string
}
export interface RecommendationItem {
  id: number; recommendationType: string; targetId: number; targetName: string
  titleOverride: string | null; sort: number; status: number; startTime: string | null
  endTime: string | null; updateTime: string
}
export interface RoleItem { roleId: number; roleName: string; roleCode: string; roleDescription: string | null; status: number; permissionIds: number[] }
export interface PermissionItem { permissionId: number; permissionName: string; permissionCode: string; moduleCode: string; permissionType: string; sort: number; status: number }
export interface AdminUserItem { userId: number; username: string; nickname: string | null; phone: string | null; status: number; lastLoginTime: string | null; roleIds: number[]; roleNames: string[] }
export interface OperationLogItem {
  id: number; adminUserId: number; adminUsername: string; moduleCode: string
  operationCode: string; operationName: string; targetType: string | null; targetId: string | null
  requestMethod: string | null; requestPath: string | null; requestIp: string | null
  beforeData: string | null; afterData: string | null; resultStatus: number
  errorMessage: string | null; createTime: string
}

export interface PageQuery { pageNum: number; pageSize: number; keyword?: string }
export interface OrderQuery extends PageQuery { orderType?: number; status?: number; userId?: number; riderId?: number; merchantId?: number; startTime?: string; endTime?: string; abnormalOnly?: boolean }
export interface UserQuery extends PageQuery { status?: number; startTime?: string; endTime?: string }
export interface RiderQuery extends PageQuery { auditStatus?: number; riderStatus?: number }
export interface ProductQuery extends PageQuery { storeId?: number; categoryId?: number; productStatus?: number; recommended?: boolean; hot?: boolean }
export interface LogQuery extends PageQuery { adminUserId?: number; moduleCode?: string; resultStatus?: number; startTime?: string; endTime?: string }

export type BannerPayload = Omit<BannerItem, 'id' | 'updateTime'>
export type NoticePayload = Omit<NoticeItem, 'id' | 'updateTime'>
export type RecommendationPayload = Omit<RecommendationItem, 'id' | 'targetName' | 'updateTime'>
