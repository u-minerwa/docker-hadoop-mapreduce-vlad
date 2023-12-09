package entity

import (
	"math/rand"
)

const (
	BitSkins   = "BitSkins"
	DMarket    = "DMarket"
	CSMoney    = "CSMoney"
	Tradeit    = "TradeIT GG"
	CSGOMarket = "CSGO Market"
	SkinWallet = "SkinWallet"
)

const (
	BitSkinsURL   = "bitskins.com"
	DMarktetURL   = "dmarket.com"
	CSMoneyURL    = "csmoney.com"
	TradeitURL    = "tradeit.gg"
	SkinWalletURL = "skinwallet.com"
)

const (
	Ak47Redline    = "AK - 47 | Redline"
	AwpAsiimov     = "AWP | Asiimov"
	AwpDragonLore  = "AWP | Dragon Lore"
	PpBison        = "PP-19 | Bison"
	FamasPule      = "Famas | Pulse"
	Ak47Vulkan     = "AK-47 | Vulkan"
	M4a1sdecimator = "M4A1-S | Decimator"
)

type SkinPriceGenerator struct {
	marketplaces map[string]string
	skins        []string
}

func NewGenerator() (error, *SkinPriceGenerator) {

	marketplaces := make(map[string]string)

	marketplaces[BitSkins] = BitSkinsURL
	marketplaces[DMarket] = DMarktetURL
	marketplaces[CSMoney] = CSMoneyURL
	marketplaces[Tradeit] = TradeitURL
	marketplaces[SkinWallet] = SkinWalletURL

	skins := make([]string, 0)

	skins = append(skins,
		Ak47Redline, AwpAsiimov, AwpDragonLore, PpBison, FamasPule, Ak47Vulkan, M4a1sdecimator,
	)

	generator := &SkinPriceGenerator{
		marketplaces: marketplaces,
		skins:        skins,
	}

	return nil, generator
}

func (gen *SkinPriceGenerator) GenerateSkinPrice() (error, SkinPrice) {
	skinFloat := rand.Float64()

	var quality string

	switch true {
	case skinFloat > 0.45:
		quality = "Battle-Scarred"
	case skinFloat > 0.38 && skinFloat <= 0.45:
		quality = "Well-Worn"
	case skinFloat > 0.15 && skinFloat <= 0.38:
		quality = "Field Tested"
	case skinFloat > 0.7 && skinFloat <= 0.15:
		quality = "Minimal Wear"
	case skinFloat >= 0 && skinFloat <= 0.7:
		quality = "Factory New"

	default:
		quality = "None"
	}

	keys := getKeys(gen.marketplaces)

	marketplaceName := keys[rand.Intn(len(keys))]

	marketplaceUrl := gen.marketplaces[marketplaceName]

	randInt := rand.Intn(len(gen.skins))

	sp := SkinPrice{
		Name:            gen.skins[randInt],
		Float:           skinFloat,
		QualityValue:    quality,
		MarketplaceName: marketplaceName,
		MarketplaceUrl:  marketplaceUrl,
		USDPrice:        rand.Int63n(99999) + 1,
	}

	return nil, sp
}

func getKeys[K comparable, V any](m map[K]V) []K {
	keys := make([]K, 0)

	for i := range m {
		keys = append(keys, i)
	}

	return keys
}
