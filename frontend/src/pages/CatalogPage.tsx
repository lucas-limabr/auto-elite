import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Vehicle, Brand, Category } from '../types';
import { VehicleCard } from '../components/VehicleCard';
import { Search, Filter, RotateCcw } from 'lucide-react';

export const CatalogPage: React.FC = () => {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Filters state
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedBrand, setSelectedBrand] = useState<string>('');
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [selectedFuel, setSelectedFuel] = useState<string>('');
  const [minPrice, setMinPrice] = useState<string>('');
  const [maxPrice, setMaxPrice] = useState<string>('');

  const fetchFiltersData = async () => {
    try {
      const [brandsRes, categoriesRes] = await Promise.all([
        api.get<Brand[]>('/brands'),
        api.get<Category[]>('/categories'),
      ]);
      setBrands(brandsRes.data);
      setCategories(categoriesRes.data);
    } catch (err) {
      console.error('Erro ao carregar marcas e categorias', err);
    }
  };

  const fetchVehicles = async () => {
    setLoading(true);
    try {
      const params: Record<string, any> = {};
      if (searchTerm.trim()) params.searchTerm = searchTerm.trim();
      if (selectedBrand) params.brandId = selectedBrand;
      if (selectedCategory) params.categoryId = selectedCategory;
      if (selectedFuel) params.fuelType = selectedFuel;
      if (minPrice) params.minPrice = minPrice;
      if (maxPrice) params.maxPrice = maxPrice;

      const response = await api.get<Vehicle[]>('/vehicles', { params });
      setVehicles(response.data);
    } catch (err) {
      console.error('Erro ao buscar veículos', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFiltersData();
    fetchVehicles();
  }, []);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchVehicles();
  };

  const handleResetFilters = () => {
    setSearchTerm('');
    setSelectedBrand('');
    setSelectedCategory('');
    setSelectedFuel('');
    setMinPrice('');
    setMaxPrice('');
    api.get<Vehicle[]>('/vehicles').then((res) => setVehicles(res.data));
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Hero / Header Clean */}
      <div className="border-b border-slate-200 pb-6">
        <h1 className="text-3xl font-bold tracking-tight text-slate-900">Catálogo de Veículos</h1>
        <p className="text-sm text-slate-500 mt-1 max-w-2xl">
          Explore nossos modelos disponíveis para pronta entrega ou agendamento de test-drive nas unidades de São Paulo e Curitiba.
        </p>
      </div>

      {/* Filter Bar */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Busca Textual */}
          <div className="md:col-span-2 relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Buscar por modelo ou marca (ex: BMW, Hilux, SUV...)"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors"
            />
          </div>

          {/* Marca */}
          <div>
            <select
              value={selectedBrand}
              onChange={(e) => setSelectedBrand(e.target.value)}
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors"
            >
              <option value="">Todas as Marcas</option>
              {brands.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name}
                </option>
              ))}
            </select>
          </div>

          {/* Categoria */}
          <div>
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors"
            >
              <option value="">Todas as Categorias</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          {/* Combustível */}
          <div>
            <select
              value={selectedFuel}
              onChange={(e) => setSelectedFuel(e.target.value)}
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors"
            >
              <option value="">Todos os Combustíveis</option>
              <option value="FLEX">Flex</option>
              <option value="GASOLINE">Gasolina</option>
              <option value="DIESEL">Diesel</option>
              <option value="HYBRID">Híbrido</option>
              <option value="ELECTRIC">Elétrico</option>
            </select>
          </div>

          {/* Faixa de Preço */}
          <div className="flex items-center gap-2 md:col-span-2">
            <input
              type="number"
              placeholder="Preço Mín (R$)"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
              className="w-1/2 px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors"
            />
            <input
              type="number"
              placeholder="Preço Máx (R$)"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
              className="w-1/2 px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors"
            />
          </div>

          {/* Actions */}
          <div className="flex items-center gap-2 md:col-span-1 justify-end">
            <button
              type="button"
              onClick={handleResetFilters}
              title="Limpar filtros"
              className="p-2 border border-slate-200 hover:bg-slate-50 text-slate-600 rounded-lg transition-colors"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
            <button
              type="submit"
              className="flex-1 flex items-center justify-center gap-1.5 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white font-medium text-sm rounded-lg transition-colors"
            >
              <Filter className="w-4 h-4" />
              Filtrar
            </button>
          </div>
        </form>
      </div>

      {/* Grid de Veículos */}
      {loading ? (
        <div className="min-h-[300px] flex items-center justify-center">
          <div className="w-8 h-8 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : vehicles.length === 0 ? (
        <div className="text-center py-20 bg-white border border-slate-200 rounded-xl">
          <p className="text-base font-semibold text-slate-800">Nenhum veículo encontrado</p>
          <p className="text-xs text-slate-500 mt-1">Tente ajustar seus termos de pesquisa ou remover alguns filtros.</p>
          <button
            onClick={handleResetFilters}
            className="mt-4 px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg transition-colors"
          >
            Limpar Todos os Filtros
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {vehicles.map((vehicle) => (
            <VehicleCard key={vehicle.id} vehicle={vehicle} />
          ))}
        </div>
      )}
    </div>
  );
};

