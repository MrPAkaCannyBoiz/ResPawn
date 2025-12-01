package org.example.respawnmarket.Service.ServiceExtensions;

import com.respawnmarket.ApprovalStatus;
import com.respawnmarket.Category;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;

import static org.example.respawnmarket.entities.enums.CategoryEnum.*;

public class CategoryExtension
{
    public static CategoryEnum toEntityCategory(com.respawnmarket.Category protoCategory)
    {
        return switch (protoCategory)
        {
            case CATEGORY_UNSPECIFIED -> null;
            case ELECTRONICS -> ELECTRONICS;
            case JEWELRY ->  JEWELRY;
            case WATCHES ->  WATCHES;
            case MUSICAL_INSTRUMENTS -> MUSICAL_INSTRUMENTS;
            case TOOLS -> TOOLS;
            case VEHICLES -> VEHICLES;
            case COLLECTIBLES -> COLLECTIBLES;
            case FASHION -> FASHION;
            case HOME_APPLIANCES -> HOME_APPLIANCES;
            case SPORTS_EQUIPMENT -> SPORTS_EQUIPMENT;
            case COMPUTERS -> COMPUTERS;
            case MOBILE_PHONES -> MOBILE_PHONES;
            case CAMERAS -> CAMERAS;
            case LUXURY_ITEMS -> LUXURY_ITEMS;
            case ARTWORK -> ARTWORK;
            case ANTIQUES -> ANTIQUES;
            case GAMING_CONSOLES -> GAMING_CONSOLES;
            case FURNITURE -> FURNITURE;
            case GOLD_AND_SILVER -> GOLD_AND_SILVER;
            case OTHER -> OTHER;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unrecognized category: " + protoCategory);
        };
    }

    public static Category toProtoCategory(CategoryEnum entityCategory)
    {
        if (entityCategory == null)
        {
            return Category.CATEGORY_UNSPECIFIED;
        }

        return switch (entityCategory)
        {
            case ELECTRONICS -> Category.ELECTRONICS;
            case JEWELRY -> Category.JEWELRY;
            case WATCHES -> Category.WATCHES;
            case MUSICAL_INSTRUMENTS -> Category.MUSICAL_INSTRUMENTS;
            case TOOLS -> Category.TOOLS;
            case VEHICLES -> Category.VEHICLES;
            case COLLECTIBLES -> Category.COLLECTIBLES;
            case FASHION -> Category.FASHION;
            case HOME_APPLIANCES -> Category.HOME_APPLIANCES;
            case SPORTS_EQUIPMENT -> Category.SPORTS_EQUIPMENT;
            case COMPUTERS -> Category.COMPUTERS;
            case MOBILE_PHONES -> Category.MOBILE_PHONES;
            case CAMERAS -> Category.CAMERAS;
            case LUXURY_ITEMS -> Category.LUXURY_ITEMS;
            case ARTWORK -> Category.ARTWORK;
            case ANTIQUES -> Category.ANTIQUES;
            case GAMING_CONSOLES -> Category.GAMING_CONSOLES;
            case FURNITURE -> Category.FURNITURE;
            case GOLD_AND_SILVER -> Category.GOLD_AND_SILVER;
            case OTHER -> Category.OTHER;
        };
    }
}
