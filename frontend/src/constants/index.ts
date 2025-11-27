export interface ProductAttribute
{
    id: number,
    attrbuteName: string,
    attributeValue: string,
    attributeGroup: string,
    unit: string,
    dataType: string,
    isSearchable: boolean,
    isFilterable: boolean,
}

export interface ProductVariant
{
    id: number,
    sku: string,
    variantName: string,
    color: string,
    size: string,
    storage: string,
    material: string,
    additionalPrice: number,
    stockQuantity: number,
    isAvailable: boolean,
    isDefault: boolean
}

export interface ProductDocument
{
    id: number,
    sku: string,
    name: string,
    slug: string,
    isActive: boolean,
    shortDescription:string,
    fulDescription: string,
    basePrice: number,
    discountedPrice: number,
    discountedPercentage: number,
    currentPrice: number,
    currency: string,
    stockQuantity: number,
    stockStatus: string,
    brandId: number,
    brandName: string,
    brandSlug: string,
    brandIsPopular: boolean,
    categoryId: number,
    categoryName: string,
    categorySlug: string,
    categoryFullPath: string,
    categoryLevel: string,
    tags: string[],
    searchKeyWords: string,
    searchBoost: number,
    viewCount: number,
    orderCount: number,
    avg_rating: number,
    createdAt: string;
    updatedAt: string;
    publishedAt: string;
    variants: ProductVariant[];
    attributes: ProductAttribute[];

}

export interface SearchParams
{
    q:string,
    page?:number,
    size?:number,
}
export interface SearchResponse
{
    query: string,
    resutls: ProductDocument[],
    total: number,
    size: number,
    tookMs: number,
    filters: string,
    hasMore: boolean
}
export interface FilterParams extends SearchParams{
    brand?: string[],
    category?: string,
    minPrice?: number,
    maxPrice?: number,
    stock?: string,
    minRating?: number,
    fuzzy?: boolean,
    sortBy?: string
}

export interface Suggestion
{
    text:string,
    type: 'product'|'brand'|'catgeory'
    id: number,
    score?: number,
    brand?: string,
    category?: string,
    price?: number,
    inStock?: boolean
}

export interface AutoCompleteResponse
{
    query: string,
    suggestions: Suggestion[],
    total: number,
    cached:boolean,
    tookMs: number
}

export interface AutoCompleteParams
{
    q?:string
}