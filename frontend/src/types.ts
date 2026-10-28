export type Product = {
  id: number
  name: string
  quantity: number
  category: string
  expirationDate: string | null
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'

export type Task = {
  id: number
  title: string
  status: TaskStatus
  createdAt: string
  dueDate: string | null
}

export type InventorySummary = {
  productCount: number
  totalUnits: number
  lowStockCount: number
}

export type StockAdjustment = {
  id: number
  productId: number
  change: number
  resultingQuantity: number
  createdAt: string
}