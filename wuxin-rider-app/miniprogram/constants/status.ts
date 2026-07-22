export const ORDER_STATUS = {
  waitingAccept: 0,
  accepted: 1,
  delivering: 2,
  waitingConfirm: 3,
  completed: 4,
  cancelled: 5,
  merchantPreparing: 6,
  waitingRider: 7,
  waitingRefund: 8
} as const;

export const RIDER_AUDIT = { pending: 0, approved: 1, rejected: 2 } as const;
export const RIDER_STATUS = { inactive: 0, enabled: 1, disabled: 2 } as const;
