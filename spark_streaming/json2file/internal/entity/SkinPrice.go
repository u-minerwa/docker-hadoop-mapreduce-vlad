package entity

type SkinPrice struct {
	Name            string  `json:"name"`
	Float           float64 `json:"float"`
	QualityValue    string  `json:"qualityValue"`
	MarketplaceName string  `json:"marketplaceName"`
	MarketplaceUrl  string  `json:"marketplaceUrl"`
	USDPrice        int64   `json:"USDPrice"`
}
