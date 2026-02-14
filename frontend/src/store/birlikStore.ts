import { create } from 'zustand'
import { Birlik } from '@/types'
import { birlikApi } from '@/api/birlik'

interface BirlikState {
  birlikler: Birlik[]
  selectedBirlik: Birlik | null
  isLoading: boolean
  error: string | null
  totalElements: number
  currentPage: number
  pageSize: number

  // Actions
  fetchBirlikler: (page?: number, size?: number) => Promise<void>
  fetchBirlikById: (id: number) => Promise<Birlik>
  setSelectedBirlik: (birlik: Birlik | null) => void
  createBirlik: (data: Parameters<typeof birlikApi.create>[0]) => Promise<Birlik>
  updateBirlik: (id: number, data: Parameters<typeof birlikApi.update>[1]) => Promise<Birlik>
  deleteBirlik: (id: number) => Promise<void>
  searchBirlik: (term: string) => Promise<Birlik[]>
}

export const useBirlikStore = create<BirlikState>((set, get) => ({
  birlikler: [],
  selectedBirlik: null,
  isLoading: false,
  error: null,
  totalElements: 0,
  currentPage: 0,
  pageSize: 20,

  fetchBirlikler: async (page = 0, size = 20) => {
    set({ isLoading: true, error: null })
    try {
      const response = await birlikApi.getAll(page, size)
      set({
        birlikler: response.data,
        totalElements: response.data.length,
        currentPage: page,
        pageSize: size,
        isLoading: false,
      })
    } catch (error) {
      set({ error: 'Birlikler yüklenirken hata oluştu', isLoading: false })
      throw error
    }
  },

  fetchBirlikById: async (id: number) => {
    set({ isLoading: true, error: null })
    try {
      const response = await birlikApi.getById(id)
      set({ selectedBirlik: response.data, isLoading: false })
      return response.data
    } catch (error) {
      set({ error: 'Birlik bilgisi yüklenirken hata oluştu', isLoading: false })
      throw error
    }
  },

  setSelectedBirlik: (birlik: Birlik | null) => {
    set({ selectedBirlik: birlik })
  },

  createBirlik: async (data) => {
    set({ isLoading: true, error: null })
    try {
      const response = await birlikApi.create(data)
      const { birlikler } = get()
      set({
        birlikler: [...birlikler, response.data],
        isLoading: false,
      })
      return response.data
    } catch (error) {
      set({ error: 'Birlik oluşturulurken hata oluştu', isLoading: false })
      throw error
    }
  },

  updateBirlik: async (id, data) => {
    set({ isLoading: true, error: null })
    try {
      const response = await birlikApi.update(id, data)
      const { birlikler } = get()
      set({
        birlikler: birlikler.map((b) => (b.id === id ? response.data : b)),
        selectedBirlik: response.data,
        isLoading: false,
      })
      return response.data
    } catch (error) {
      set({ error: 'Birlik güncellenirken hata oluştu', isLoading: false })
      throw error
    }
  },

  deleteBirlik: async (id) => {
    set({ isLoading: true, error: null })
    try {
      await birlikApi.delete(id)
      const { birlikler, selectedBirlik } = get()
      set({
        birlikler: birlikler.filter((b) => b.id !== id),
        selectedBirlik: selectedBirlik?.id === id ? null : selectedBirlik,
        isLoading: false,
      })
    } catch (error) {
      set({ error: 'Birlik silinirken hata oluştu', isLoading: false })
      throw error
    }
  },

  searchBirlik: async (term: string) => {
    try {
      const response = await birlikApi.search(term)
      return response.data
    } catch (error) {
      throw error
    }
  },
}))
