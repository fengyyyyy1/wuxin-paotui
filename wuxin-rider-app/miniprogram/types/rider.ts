export interface RiderApplyRequest {
  realName: string;
  idCard: string;
  idCardFront: string;
  idCardBack: string;
}

export interface RiderApplyResult { riderId: number; auditStatus: number; auditStatusText: string; applyTime: string; }

export interface RiderProfile {
  riderId: number;
  userId: number;
  username: string;
  nickname: string | null;
  avatar: string | null;
  phone: string | null;
  realName: string;
  idCardMasked: string;
  idCardFront: string;
  idCardBack: string;
  auditStatus: number;
  auditStatusText: string;
  riderStatus: number;
  riderStatusText: string;
  rejectReason: string | null;
  applyTime: string;
  updateTime: string;
}

export interface RiderStatistics {
  riderId: number;
  todayCompletedCount: number;
  weekCompletedCount: number;
  monthCompletedCount: number;
  totalCompletedCount: number;
}

export interface RiderRanking {
  rank: number;
  riderId: number;
  riderUserId: number;
  riderName: string;
  avatar: string | null;
  completedOrderCount: number;
}
