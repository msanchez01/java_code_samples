package com.spfsolutions.ioms.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.Where;
import com.spfsolutions.ioms.annotations.Transactional;
import com.spfsolutions.ioms.common.ItemClassificationEnum;
import com.spfsolutions.ioms.data.CategoryEntity;
import com.spfsolutions.ioms.data.ClassificationEntity;
import com.spfsolutions.ioms.data.CombinationEntity;
import com.spfsolutions.ioms.data.CombinationElementEntity;
import com.spfsolutions.ioms.data.CombinationItemEntity;
import com.spfsolutions.ioms.data.CombinationPricingTypeEntity;
import com.spfsolutions.ioms.data.DaysOfWeekEntity;
import com.spfsolutions.ioms.data.DeliveryRateEntity;
import com.spfsolutions.ioms.data.EntreeCombinationEntity;
import com.spfsolutions.ioms.data.GeofenceAreaEntity;
import com.spfsolutions.ioms.data.ItemEntity;
import com.spfsolutions.ioms.data.ItemOptionEntity;
import com.spfsolutions.ioms.data.MenuEntity;
import com.spfsolutions.ioms.data.MenuItemPlacementEntity;
import com.spfsolutions.ioms.data.MenuSpecialPlacementEntity;
import com.spfsolutions.ioms.data.MenuTypeEntity;
import com.spfsolutions.ioms.data.OptionEntity;
import com.spfsolutions.ioms.data.OptionGroupEntity;
import com.spfsolutions.ioms.data.OptionValueEntity;
import com.spfsolutions.ioms.data.PromotionalTextEntity;
import com.spfsolutions.ioms.data.ScheduleEntity;
import com.spfsolutions.ioms.data.ServerSettingEntity;
import com.spfsolutions.ioms.data.SpecialEntity;
import com.spfsolutions.ioms.data.TaxCategoryEntity;
import com.spfsolutions.ioms.data.TaxRateEntity;
import com.spfsolutions.ioms.data.GeoFenceZipcodeEntity;
import com.spfsolutions.ioms.data.TransactionOrderEntity;
import com.spfsolutions.ioms.exceptions.CustomException;
import com.spfsolutions.ioms.exceptions.MaximumOrderDateExceededException;
import com.spfsolutions.ioms.exceptions.MissingTaxRateException;
import com.spfsolutions.ioms.exceptions.ResourceNotFoundException;
import com.spfsolutions.ioms.exceptions.ZipCodeOutOfRangeException;
import com.spfsolutions.ioms.models.MenuModel;
import com.spfsolutions.ioms.models.RestaurantModel;
import com.spfsolutions.ioms.objects.Category;
import com.spfsolutions.ioms.objects.Classification;
import com.spfsolutions.ioms.objects.CombinationElement;
import com.spfsolutions.ioms.objects.CombinationItem;
import com.spfsolutions.ioms.objects.ContactInformation;
import com.spfsolutions.ioms.objects.DeliveryRate;
import com.spfsolutions.ioms.objects.DynamicBranding;
import com.spfsolutions.ioms.objects.MenuCombination;
import com.spfsolutions.ioms.objects.MenuItem;
import com.spfsolutions.ioms.objects.OptionGroup;
import com.spfsolutions.ioms.objects.OptionValue;
import com.spfsolutions.ioms.objects.PromotionalText;
import com.spfsolutions.ioms.objects.Rate;
import com.spfsolutions.ioms.objects.Schedule;
import com.spfsolutions.ioms.objects.Calendarization;
import com.spfsolutions.ioms.objects.Option;
import com.spfsolutions.ioms.objects.Special;
import com.spfsolutions.ioms.objects.TaxRate;
import com.spfsolutions.ioms.objects.ZipcodeRange;
import com.spfsolutions.ioms.objects.TippingSettings;
import com.spfsolutions.ioms.objects.ValidityTimeRange;
import com.spfsolutions.ioms.objects.VoiceOrdering;
import com.spfsolutions.ioms.utils.PeriodConverter;

public class MenuService extends BaseService{
    
    @Autowired
    RestaurantService restaurantService;
    
    @Autowired
    ICCService iccService;
    
    @Autowired
    Dao<GeoFenceZipcodeEntity, Integer> geofenceZipcodeDao;

    @Autowired
    TransactionManager transactionManager;    
    
    @Autowired
    Dao<MenuEntity, Integer> menuDao;
    
    @Autowired
    Dao<ScheduleEntity, Integer> scheduleDao;
    
    @Autowired
    Dao<ItemEntity, Integer> itemDao;
    
    @Autowired
    Dao<CombinationEntity, Integer> combinationDao;
    
    @Autowired
    Dao<CombinationElementEntity, Integer> combinationElementDao;
    
    @Autowired
    Dao<EntreeCombinationEntity, Integer> entreeCombinationDao;
    
    @Autowired
    Dao<CombinationItemEntity, Integer> combinationItemDao;
    
    @Autowired
    Dao<OptionGroupEntity, Integer> optionGroupDao;
    
    @Autowired
    Dao<OptionValueEntity, Integer> optionValueDao;
        
    @Autowired
    Dao<OptionEntity, Integer> optionDao;
    
    @Autowired
    Dao<ItemOptionEntity, Integer> itemOptionDao;
    
    @Autowired
    Dao<MenuItemPlacementEntity, Integer> menuItemPlacementDao;
    
    @Autowired
    Dao<SpecialEntity, Integer> specialDao;
    
    @Autowired
    Dao<MenuTypeEntity, Integer> menuTypeDao;
    
    @Autowired
    Dao<MenuSpecialPlacementEntity, Integer> menuSpecialPlacementDao;
    
    @Autowired
    Dao<ServerSettingEntity, Integer> serverSettingDao;
    
    @Autowired
    Dao<ClassificationEntity, Integer> classificationDao;
    
    @Autowired
    Dao<CombinationPricingTypeEntity, Integer> combinationPricingTypeDao;
    
    @Autowired
    Dao<CategoryEntity, Integer> categoryDao;
    
    @Value("${paymentgateway.merchantusername}")
    String merchantUsername;
    
    @Value("${paymentgateway.gatewayType}")
    String gatewayType;
    
    @Value("${paymentgateway.merchantpassword}")
    String merchantPassword;
    
    @Value("${paymentgateway.locationitem}")
    String merchantLocationItem;    
    
    @Value("${paymentgateway.auth_login_id}")
    String merchantAuthLogin;
    
    @Value("${paymentgateway.auth_tran_key}")
    String merchantAuthTranKey;
    
    @Value("${paymentgateway.auth_url}")
    String merchantAuthUrl;
    
    @Value("${branding.enabledynamicbranding}")
    boolean dynamicBrandingEnabled;
    
    @Value("${branding.organizationcolor}")
    String organizationColor;
    @Value("${branding.locationdominantcolor}")
    String locationDominantColor;
    @Value("${branding.categorydividercolor}")
    String categoryDividerColor;
    @Value("${branding.titlestripcolor}")
    String titleStripColor;
    @Value("${branding.titlestriptextcolor}")
    String titleStripTextColor;
    @Value("${branding.backgroundhomescreencolor}")
    String backgroundHomeScreenColor;
    
    @Value("${branding.coverpictureextraextrahigh}")
    String coverPictureExtraExtraHigh;
    @Value("${branding.coverpictureextrahigh}")
    String coverPictureExtraHigh;
    @Value("${branding.coverpicturehigh}")
    String coverPictureHigh;
    @Value("${branding.coverpicturemedium}")
    String coverPictureMedium;
    @Value("${branding.coverpicturelow}")
    String coverPictureSmall;
    
    @Value("${branding.logopictureextraextrahigh}")
    String logoPictureExtraExtraHigh;
    @Value("${branding.logopictureextrahigh}")
    String logoPictureExtraHigh;
    @Value("${branding.logopicturehigh}")
    String logoPictureHigh;
    @Value("${branding.logopicturemedium}")
    String logoPictureMedium;
    @Value("${branding.logopicturelow}")
    String logoPictureSmall;
    
    @Value("${branding.mainmenu.usual}")
    String mainMenuUsual;
    @Value("${branding.mainmenu.favorites}")
    String mainMenuFavorites;
    @Value("${branding.mainmenu.pastorders}")
    String mainMenuPastOrders;
    @Value("${branding.mainmenu.placeorder}")
    String mainMenuPlaceOrder;
    @Value("${branding.mainmenu.operationhours}")
    String mainMenuOperationHours;
    
    @Value("${voiceOrdering.defaultSpecialtyMenuId}")
    int defaultSpecialtyMenuId;
    
    @Value("${voiceOrdering.defaultCustomItemId}")
    int defaultCustomItemId;
    
    @Autowired
    Dao<TransactionOrderEntity, Integer> transactionOrderDao;
    
    @Autowired
    Dao<PromotionalTextEntity, Integer> promotionalTextDao;
    
    @Autowired
    Dao<TaxRateEntity, Integer> taxRateDao;
    
    @Autowired
    Dao<DeliveryRateEntity, Integer> deliveryRateDao;
    
    @Autowired
    SecureRandom randomGenerator;
    
    private int utcOffSet;
    
    private static final Logger log = Logger.getLogger(MenuService.class);
    
    public MenuService(){
    }    
    
    
    
    public Classification getClassification(int classificationId)
    {
        Classification returnedClassification = null;
        try {
            ClassificationEntity classificationEntity = classificationDao.queryForId(classificationId);
            if(classificationEntity == null) throw new ResourceNotFoundException("The classificationId wasn't found.");
            
            returnedClassification = new Classification(classificationEntity.getId(), classificationEntity.getName());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnedClassification;
    }
    
    public List<Classification> getClassifications()
    {
        List<Classification> returnedClassifications = new ArrayList<Classification>();
        
        try {            
            List<ClassificationEntity> classificationEntities = classificationDao.queryForAll();
            
            for(Iterator<ClassificationEntity> i = classificationEntities.iterator(); i.hasNext(); ) {
                ClassificationEntity item = i.next();
                returnedClassifications.add(new Classification(item.getId(), item.getName()));
            }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnedClassifications;
    }
    
    public Category getCategory(int categoryId)
    {
        Category returnedCategory = null;
        try {
            CategoryEntity categoryEntity = categoryDao.queryForId(categoryId);
            if(categoryEntity == null) throw new ResourceNotFoundException("the categoryId wasn't found.");
            
            returnedCategory = new Category(categoryEntity.getId(), categoryEntity.getName());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnedCategory;
    }
    
    public Category deleteCategory(int categoryId)
    {
        Category returnedCategory = null;
        try {
            CategoryEntity categoryEntity = categoryDao.queryForId(categoryId);
            if(categoryEntity == null) throw new ResourceNotFoundException("the categoryId wasn't found.");
            if(categoryEntity.getItems().size() > 0) throw new CustomException("Category is being used by current menu items."); 
            
            categoryEntity.getSchedules().clear();
            categoryEntity.setEnabled(false);
            categoryDao.createOrUpdate(categoryEntity);
            
            returnedCategory = new Category(categoryEntity.getId(), categoryEntity.getName());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnedCategory;
    }
    
    public List<MenuItem> getItemsPerCategory(int categoryId)
    {
        CategoryEntity categoryEntity;
        List<MenuItem> menuItemList = new ArrayList<MenuItem>();
        try {
            categoryEntity = categoryDao.queryForId(categoryId);
            if(categoryEntity == null) throw new ResourceNotFoundException("the categoryId wasn't found.");
            
            ForeignCollection<ItemEntity> items = categoryEntity.getItems();
            if(items.size() > 0)
            {
                Iterator<ItemEntity> itemsIterator = items.iterator();
                while(itemsIterator.hasNext())
                {
                    ItemEntity itemEntity = itemsIterator.next();
                    menuItemList.add(new MenuItem(itemEntity.getId(), itemEntity.getName(), itemEntity.getSize(), itemEntity.getDescription()));
                }
            }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return menuItemList;
        
    }
    
    public Category editCategory(Category categoryModel)
    {
        CategoryEntity categoryEntity;
        try {
            categoryEntity = categoryDao.queryForId(categoryModel.getId());
            if(categoryEntity == null) throw new ResourceNotFoundException("No Category was found for the given categoryId.");
            
            categoryEntity.setName(categoryModel.getName());
            categoryEntity.setDisplaySequence(categoryModel.getDisplaySequence());
            
            categoryEntity.getSchedules().clear();
            for(Integer weekDayId: categoryModel.getCategoryValidityDateRange().getSchedule().getDaysOfWeek())
            {
                ScheduleEntity schedule = new ScheduleEntity(null, null, categoryEntity, new DaysOfWeekEntity(weekDayId));
                scheduleDao.createOrUpdate(schedule);
            }
            
            categoryDao.createOrUpdate(categoryEntity);
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return categoryModel;    
    }
    
    public Category addCategory(Category categoryModel)
    {
        CategoryEntity categoryEntity;
        try {
            categoryEntity = new CategoryEntity(categoryModel.getName(), categoryModel.getDisplaySequence()); 
            categoryEntity.setEnabled(true);
            categoryDao.createOrUpdate(categoryEntity);
            for(Integer weekDayId: categoryModel.getCategoryValidityDateRange().getSchedule().getDaysOfWeek())
            {
                ScheduleEntity schedule = new ScheduleEntity(null, null, categoryEntity, new DaysOfWeekEntity(weekDayId));
                scheduleDao.createOrUpdate(schedule);
            }
            categoryModel.setId(categoryEntity.getId());
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return categoryModel;    
    }
    
    public List<Category> getCategories()
    {
        List<Category> returnedCategories = new ArrayList<Category>();
        
        try {            
            List<CategoryEntity> categoryEntities = categoryDao.queryForEq("Enabled", true);
            
            for(Iterator<CategoryEntity> i = categoryEntities.iterator(); i.hasNext(); ) {
                CategoryEntity categoryEntity = i.next();
                Schedule weeklySchedule = getWeeklySchedule("CategoryId", categoryEntity.getId());
                returnedCategories.add(new Category(categoryEntity.getId(), categoryEntity.getName(), categoryEntity.getDisplaySequence(), new Calendarization(weeklySchedule)));
            }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnedCategories;
    }
    
    public MenuModel readImportMenuXml(MultipartFile file, String fileName)
    {
        MenuModel menu = null;
         
        try 
        {            
            InputStream inputStream = file.getInputStream(); 
            
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer);
            String xmlText = writer.toString();
               
            JAXBContext jaxbContext = JAXBContext.newInstance(MenuModel.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            StringReader reader = new StringReader(xmlText);
            menu = (MenuModel) unmarshaller.unmarshal(reader);        
                           
        } catch (IOException e) {
             e.printStackTrace();
         } catch(JAXBException e){
                  e.printStackTrace();
         }
        return menu;

    }
    
    public List<MenuItem> searchMenuItems(String searchKeyword, Date futureDate) throws SQLException{
        
        if(utcOffSet == 0){
            utcOffSet = restaurantService.getCurrentUtcOffSet();
        }
        int currentDayOfWeekId = 0;
        if(futureDate != null)
        {
            currentDayOfWeekId = new DateTime(futureDate, DateTimeZone.UTC).plusMillis(utcOffSet).getDayOfWeek();
        }
        else
        {
            currentDayOfWeekId = new DateTime(DateTimeZone.UTC).plusMillis(utcOffSet).getDayOfWeek();
        }
        
        
        List<MenuItem> menuItems = new ArrayList<MenuItem>();
        List<ItemEntity> itemEntities = itemDao.query(itemDao.queryBuilder().where().like("Name",  "%" + searchKeyword + "%").or().like("Description",  "%" + searchKeyword + "%").prepare());
        
        for(ItemEntity itemEntity : itemEntities){
            MenuItemPlacementEntity menuItemPlacementEntity = menuItemPlacementDao.queryForFirst(menuItemPlacementDao.queryBuilder().where().eq("ItemId", itemEntity).and().eq("Enabled", true).prepare());
            
            if(menuItemPlacementEntity == null || !isMenuAvailable(menuItemPlacementEntity.getMenu(), futureDate, currentDayOfWeekId )){
                continue;
            }
            menuItems.add(new MenuItem(menuItemPlacementEntity.getMenu().getId(), menuItemPlacementEntity.getItem().getId()));
        }
        return menuItems;
    }
    
    public MenuItem getMenuItem(int itemId)
    {
        MenuItem returnedItem = null;
        try {
            ItemEntity itemEntity = itemDao.queryForId(itemId);
            if(itemEntity == null) throw new ResourceNotFoundException("No item found for the given id");
            
            returnedItem = new MenuItem(itemEntity.getId(), itemEntity.getName(), itemEntity.getSize(), itemEntity.getCategory(), itemEntity.getClassification().getName(), itemEntity.getClassification().getId(), itemEntity.getDescription(), itemEntity.getPrice(), itemEntity.getAllergyDescription(), null);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(), e);
        }
        //org.apache.tomcat.jdbc.pool.DataSource d = new
        
        return returnedItem;
        
    }
    
    public MenuItem addMenuItem(final MenuItem item)
    {        
        final ItemEntity itemEntity = new ItemEntity(new ClassificationEntity(item.getClassificationId()), new CategoryEntity(item.getCategoryId()), item.getItemName(), item.getItemSize(), item.getDescription(), item.getAllergyDescription(), item.getItemPrice());
        
        try {            
            
            transactionManager.callInTransaction(new Callable<Void>() {
                public Void call() throws Exception {
                
                    MenuEntity menuEntity = menuDao.queryForId(item.getMenuId());
                    if(menuEntity == null) throw new ResourceNotFoundException("No menu could be found with the provided MenuId");
                    
                    MenuItemPlacementEntity menuItemPlacementEntity = new MenuItemPlacementEntity(new MenuEntity(item.getMenuId()), itemEntity);
                    itemEntity.setTaxCategoryEntity(new TaxCategoryEntity(item.getTaxCategoryId()));
                    itemDao.create(itemEntity);
                    
                    for(Integer weekDayId: item.getItemValidityDateRange().getSchedule().getDaysOfWeek())
                    {
                        ScheduleEntity schedule = new ScheduleEntity(null, itemEntity, null, new DaysOfWeekEntity(weekDayId));
                        scheduleDao.createOrUpdate(schedule);
                    }
                    menuItemPlacementEntity.setDisplaySequence(item.getDisplaySequence());    
                    menuItemPlacementEntity.setEnabled(true);
                    menuItemPlacementDao.create(menuItemPlacementEntity);
                    
                    if(item.getOptions() != null && !item.getOptions().isEmpty())
                    {
                        if(itemEntity.getItemOptions() != null)
                            itemEntity.getItemOptions().clear();    
                            
                        for(Option option : item.getOptions() ){
                            OptionEntity optionEntity = optionDao.queryForId(option.getId());
                            if(optionEntity != null)
                            {
                                ItemOptionEntity itemOptionEntity = new ItemOptionEntity(itemEntity, optionEntity);
                                itemOptionDao.createOrUpdate(itemOptionEntity);
                            }                    
                        }
                        
                    }
                    if(itemEntity.getClassification().getId() == ItemClassificationEnum.ENTREE.getCode() && item.getCombinations() != null && !item.getCombinations().isEmpty())
                    {
                        if(itemEntity.getEntreeCombinations() != null)
                            itemEntity.getEntreeCombinations().clear();        
                        
                        for(MenuCombination combination : item.getCombinations() ){
                            CombinationEntity combinationEntity = combinationDao.queryForId(combination.getCombinationId());
                            if(combinationEntity != null)
                            {
                                EntreeCombinationEntity entreeCombinationEntity = new EntreeCombinationEntity(itemEntity, combinationEntity);
                                entreeCombinationDao.createOrUpdate(entreeCombinationEntity);
                            }                    
                        }
                        
                    }            
                    
                    item.setItemId(itemEntity.getId());
                    
                    
                    return null;
                }});
            
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new CustomException(e.getMessage(), e);
        }
        return item;
    }
    
    public List<MenuItem> editMenuItems(List<MenuItem> menuItems)
    {
        for(MenuItem menuItem: menuItems)
        {
            editMenuItem(menuItem);
        }
        return menuItems;
        
    }
    
    public MenuItem changeMenuItemStatus(MenuItem item) throws SQLException{
        
        MenuItemPlacementEntity menuItemPlacementEntity = menuItemPlacementDao.
                queryForFirst(menuItemPlacementDao.queryBuilder().where().eq("ItemId", item.getItemId()).prepare());
        
        if(menuItemPlacementEntity != null){
            menuItemPlacementEntity.setEnabled(item.isEnabled());
            menuItemPlacementDao.createOrUpdate(menuItemPlacementEntity);
        }
        
        return item;
        
    }
    
    public MenuItem editMenuItem(MenuItem item)
    {
        try {
            ItemEntity itemEntity = itemDao.queryForId(item.getItemId());
            if(itemEntity == null) throw new ResourceNotFoundException("Item not found for Id " + item.getItemId());
            
            item.setCategoryId(itemEntity.getCategoryEntity().getId());
            
            if(item.getAllergyDescription() != null) itemEntity.setAllergyDescription(item.getAllergyDescription());
            if(item.getCategoryId() != 0) itemEntity.setCategoryEntity(new CategoryEntity(item.getCategoryId()));
            if(item.getClassificationId() != 0) itemEntity.setClassification(new ClassificationEntity(item.getClassificationId()));
            if(item.getDescription() != null) itemEntity.setDescription(item.getDescription());
            if(item.getItemPrice() != BigDecimal.ZERO) itemEntity.setPrice(item.getItemPrice());
            if(item.getItemName() != null) itemEntity.setName(item.getItemName());
            
            itemEntity.getSchedules().clear();
            for(Integer weekDayId: item.getItemValidityDateRange().getSchedule().getDaysOfWeek())
            {
                ScheduleEntity schedule = new ScheduleEntity(null, itemEntity, null, new DaysOfWeekEntity(weekDayId));
                scheduleDao.createOrUpdate(schedule);
            }
            
            if(itemEntity.getClassification().getId() == ItemClassificationEnum.ENTREE.getCode() && item.getCombinations() != null && !item.getCombinations().isEmpty())
            {                    
                itemEntity.getEntreeCombinations().clear();
                
                for(MenuCombination combination : item.getCombinations() ){
                    CombinationEntity combinationEntity = combinationDao.queryForId(combination.getCombinationId());
                    if(combinationEntity != null)
                    {
                        itemEntity.getEntreeCombinations().add(new EntreeCombinationEntity(itemEntity, combinationEntity));
                    }                    
                }
            }
            
            if(item.getOptions() == null || item.getOptions().isEmpty()) 
            {
                itemEntity.getItemOptions().clear();
            }
            else
            if(item.getOptions() != null && !item.getOptions().isEmpty())
            {
                itemEntity.getItemOptions().clear();
                
                for(Option option : item.getOptions() ){
                    OptionEntity optionEntity = optionDao.queryForId(option.getId());
                    if(optionEntity != null)
                    {
                        itemEntity.getItemOptions().add(new ItemOptionEntity(itemEntity, optionEntity));
                    }                    
                }
            }
            
            if(item.getMenuId() != 0)
            {
                MenuEntity menuEntity = menuDao.queryForId(item.getMenuId());
                if(menuEntity == null) throw new ResourceNotFoundException("No menu could be found with the menuId provided.");
                
                MenuItemPlacementEntity menuItemPlacementEntity = menuItemPlacementDao.
                        queryForFirst(menuItemPlacementDao.queryBuilder().where().eq("ItemId", item.getItemId()).prepare());
                if(menuItemPlacementEntity == null){
                    menuItemPlacementEntity = new MenuItemPlacementEntity(menuEntity, itemEntity);
                } else {
                    menuItemPlacementEntity.setMenu(menuEntity);
                }
                menuItemPlacementEntity.setDisplaySequence(item.getDisplaySequence());
                menuItemPlacementEntity.setEnabled(true);
                item.setEnabled(true);
                menuItemPlacementDao.createOrUpdate(menuItemPlacementEntity);
                
            }
            itemEntity.setTaxCategoryEntity(new TaxCategoryEntity(item.getTaxCategoryId()));
            itemDao.createOrUpdate(itemEntity);
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new CustomException(e.getMessage(), e);
        }
        return item;
    }
    
    public void deleteMenuItem(int itemId)
    {
        //This method just removes the item from menuitemplacement table, doesn't remove from items table because we need to keep record for accounting purposes
        try {
            List<MenuItemPlacementEntity> menuItemPlacementEntities = menuItemPlacementDao.query(menuItemPlacementDao.queryBuilder().where().eq("ItemId", itemId).prepare());
            
            for(MenuItemPlacementEntity itemPlacementEntity : menuItemPlacementEntities)
            {
                ItemEntity itemEntity = itemPlacementEntity.getItem();
                
                itemEntity.getSchedules().clear();
                
                itemEntity.setCategoryEntity(null);
                itemDao.update(itemEntity);
            }
            
            
            menuItemPlacementDao.delete(menuItemPlacementEntities);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new CustomException(e.getMessage(), e);
        }
    }
    
    public Special getSpecialForMenuItem(List<Special> currentSpecials, int menuItemId)
    {
        if(currentSpecials == null) return null;
        
        for(Special currentSpecial: currentSpecials)
            if(currentSpecial.getMenuItemId() == menuItemId)
                return currentSpecial;
        
        return null;
    }
    
    private List<MenuItem> getMenuItems(MenuEntity menuEntity, int dayOfWeekId, boolean isApiCall, List<Integer> currentCategories, Date futureOrderDate)
    {
        List<MenuItem> items = new ArrayList<MenuItem>();
        
        List<Special> currentSpecials = null;
        if(isApiCall)
        {
            currentSpecials = getSpecials(menuEntity, true, futureOrderDate);
        }
        
        for(MenuItemPlacementEntity itemPlacementEntity: menuEntity.getMenuitemplacements())
        {
            ItemEntity itemEntity = itemPlacementEntity.getItem();
            if(isApiCall && (!currentCategories.contains(itemEntity.getCategoryEntity().getId()) || !itemPlacementEntity.isEnabled()))
            {
                continue;
            }
            Schedule menuItemSchedule = null;
            
            try {
                menuItemSchedule = getWeeklySchedule("ItemId", itemEntity.getId());
                if(isApiCall && dayOfWeekId != 0 && !menuItemSchedule.getDaysOfWeek().contains(dayOfWeekId)) continue;
                
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            MenuItem menuItem = new MenuItem(itemEntity.getId(), itemEntity.getName(), itemEntity.getSize(), itemEntity.getCategoryEntity() == null ?  itemEntity.getCategory() : itemEntity.getCategoryEntity().getName(), itemEntity.getClassification().getName(),itemEntity.getClassification().getId(), itemEntity.getDescription(), itemEntity.getPrice(), itemEntity.getAllergyDescription(), new Calendarization(menuItemSchedule));
            
            menuItem.setTaxCategoryId(itemEntity.getTaxCategoryEntity() == null ? 0 : itemEntity.getTaxCategoryEntity().getId());
            Special currentSpecial = getSpecialForMenuItem(currentSpecials, itemEntity.getId());
            if(currentSpecial != null)
            {
                menuItem.setItemPrice(currentSpecial.getSpecialPrice());
                menuItem.setSpecial(true);
                menuItem.setSpecialTitle(currentSpecial.getSpecialTitle());
                menuItem.setSpecialDescription(currentSpecial.getSpecialDescription());
                menuItem.setSpecialHighlighted(currentSpecial.getHighlighted());
            }
            
            menuItem.setEnabled(itemPlacementEntity.isEnabled());
            menuItem.setDisplaySequence(itemPlacementEntity.getDisplaySequence());
            menuItem.setCategoryId(itemEntity.getCategoryEntity() == null ?  0: itemEntity.getCategoryEntity().getId());
            menuItem.setCategoryOrderSequence(itemEntity.getCategoryEntity() == null ?  0: itemEntity.getCategoryEntity().getDisplaySequence());
            Iterator<ItemOptionEntity> itemOptionIterator = itemEntity.getItemOptions().iterator();    
            List<Option> sortedOptions = null;
            if(itemOptionIterator.hasNext()){
                menuItem.setOptions(new ArrayList<Option>());
                sortedOptions = new ArrayList<Option>();
            }
            
            while(itemOptionIterator.hasNext())
            {
                sortedOptions.add(getMenuOption(itemOptionIterator.next().getOption()));                
            }    
            
            if(sortedOptions != null && !sortedOptions.isEmpty()){
                
                Collections.sort(sortedOptions, new Comparator<Option>() {
                    public int compare(Option o1, Option o2) {
                        
                        if(o1.getDisplaySequence().intValue() == o2.getDisplaySequence().intValue()) 
                            return o1.getOptionName().compareTo(o2.getOptionName());
                        
                        else if(o1.getDisplaySequence() > o2.getDisplaySequence())
                            return 1;
                        
                        return -1;
                    }
                });
                
                for(int i = 0 ; i < sortedOptions.size(); i++){
                    menuItem.getOptions().add(new Option(sortedOptions.get(i).getId()));
                }
            }
            
            Iterator<EntreeCombinationEntity> entreeCombinationIterator = itemEntity.getEntreeCombinations().iterator();    
            if(entreeCombinationIterator.hasNext())
                menuItem.setCombinations(new ArrayList<MenuCombination>());
            
            while(entreeCombinationIterator.hasNext())
            {
                menuItem.getCombinations().add(new MenuCombination(entreeCombinationIterator.next().getCombination().getId()));
            }    
            
            items.add(menuItem);
        }
        
        
        Collections.sort(items, new Comparator<MenuItem>() {
            public int compare(MenuItem m1, MenuItem m2) {
                
                int comb = -1;
                if(m1.getCategoryOrderSequence() == m2.getCategoryOrderSequence())
                {
                    comb = m1.getCategory().compareTo(m2.getCategory());
                }
                if(comb == 0)
                {
                    if(m1.getDisplaySequence().intValue() == m2.getDisplaySequence().intValue()) 
                        return m1.getItemName().compareTo(m2.getItemName());
                    
                    else if(m1.getDisplaySequence() > m2.getDisplaySequence())
                        return 1;
                    
                    return -1;
                    
                }
                else if(m1.getCategoryOrderSequence() > m2.getCategoryOrderSequence())
                {
                    return 1;
                }
                     
                return comb;
            }
        });
        
        return items;
    }
    
    public Special getSpecial(int specialId)
    {
        Special special = null;
        try {
            SpecialEntity specialEntity = specialDao.queryForId(specialId);
            if(specialEntity == null) throw new ResourceNotFoundException("No Special found for the given specialId");
            
            List<MenuSpecialPlacementEntity> specialPlacementEntityList = menuSpecialPlacementDao.queryForEq("SpecialId", specialId);
            special = new Special(specialId, "", specialEntity.getDisplaySequence(), specialEntity.getDescription(), 
                    new Calendarization(new DateTime(specialEntity.getValidFrom()), new DateTime(specialEntity.getValidThrough())), 
                    new ValidityTimeRange(specialEntity.getValidTimeFrom(), specialEntity.getValidTimeThrough()), 
                    specialPlacementEntityList.isEmpty() ? 0 : specialPlacementEntityList.get(0).getId(), 
                    specialEntity.getPrice(), specialEntity.isHighlighted());
            
            Iterator<ItemOptionEntity> itemOptionIterator = specialEntity.getItem().getItemOptions().iterator();
            Iterator<EntreeCombinationEntity> itemCombinationIterator = specialEntity.getItem().getEntreeCombinations().iterator();
            
            if(itemOptionIterator.hasNext())
                special.setMenuItemOptionList( new ArrayList<Option>());
            while(itemOptionIterator.hasNext())
            {                        
                ItemOptionEntity optionEntity = itemOptionIterator.next();
                special.getMenuItemOptionList().add(new Option(optionEntity.getOption().getId()));
            }
            
            if(itemCombinationIterator.hasNext())
                special.setCombinationList( new ArrayList<MenuCombination>());
            while(itemCombinationIterator.hasNext())
            {                        
                EntreeCombinationEntity entreeCombinationEntity = itemCombinationIterator.next();
                special.getCombinationList().add(new MenuCombination(entreeCombinationEntity.getCombination().getId()));
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return special;
        
    }
    
    public List<Special> getSpecials(int menuId)
    {
        try {
            MenuEntity menu = menuDao.queryForId(menuId);
            
            if(menu == null) return null;
            
            ServerSettingEntity restaurantSettings = serverSettingDao.queryForFirst(serverSettingDao.queryBuilder().prepare());
            DateTimeZone zone = DateTimeZone.forID(restaurantSettings.getTimeZoneId());
            utcOffSet = zone.getOffset(new DateTime(DateTimeZone.UTC).withTime(0, 0, 0,
                    0));
            
            return getSpecialsForPortal(menu);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }    
    

    public void deleteSpecial(int specialId)
    {
        try {
            List<MenuSpecialPlacementEntity> menuSpecialPlacementEntities = menuSpecialPlacementDao.queryForEq("SpecialId", specialId);
            
            menuSpecialPlacementDao.delete(menuSpecialPlacementEntities);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Special createSpecial(Special model)
    {
        try {
            
            ServerSettingEntity settings = serverSettingDao.queryForFirst(serverSettingDao.queryBuilder().prepare());
            DateTimeZone zone = DateTimeZone.forID(settings.getTimeZoneId());
            int utcOffSet = zone.getOffset(new DateTime(DateTimeZone.UTC).withTime(0, 0, 0,
                    0));
            
            MenuItemPlacementEntity menuItemPlacement = menuItemPlacementDao.
                    queryForFirst(menuItemPlacementDao.queryBuilder().
                            where().eq("ItemId", model.getMenuItemId()).
                            and().eq("MenuId", model.getMenuId()).prepare());
            if(menuItemPlacement == null) throw new IllegalArgumentException("The itemId and menuId given do not match.");
            
            //ItemEntity itemEntity = menuItemPlacement.getItem();
            
            SpecialEntity specialEntity = new SpecialEntity(model.getSpecialTitle(), 
                    model.getSpecialDescription(), 
                    model.getSpecialValidityDateRange().getStartDate().toDate(), 
                    model.getSpecialValidityDateRange().getEndDate().toDate(), 
                    new LocalTime(model.getSpecialValidityTimeRange().getStartTime()).plusMillis(utcOffSet*-1).toString(), 
                    new LocalTime(model.getSpecialValidityTimeRange().getEndTime()).plusMillis(utcOffSet*-1).toString(),
                    menuItemPlacement.getItem(), 
                    model.getSpecialPrice());
            specialEntity.setStatus("Active");
            specialEntity.setHighlighted(model.getHighlighted());
            specialEntity.setDisplaySequence(model.getDisplaySequence());
            
            specialDao.createOrUpdate(specialEntity);
            //itemDao.createOrUpdate(itemEntity);
            model.setSpecialId(specialEntity.getId());
            menuSpecialPlacementDao.createOrUpdate(new MenuSpecialPlacementEntity(new MenuEntity(model.getMenuId()), specialEntity));
            iccService.sendSpecials(restaurantService.getIccSpecials());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return model;
    }
    
    public Special editSpecial(Special special)
    {
        try {
            
            ServerSettingEntity restaurantSettings = serverSettingDao.queryForFirst(serverSettingDao.queryBuilder().prepare());
            DateTimeZone zone = DateTimeZone.forID(restaurantSettings.getTimeZoneId());
            int utcOffSet = zone.getOffset(new DateTime(DateTimeZone.UTC).withTime(0, 0, 0,
                    0));
            
            SpecialEntity specialEntity = specialDao.queryForId(special.getSpecialId());
            specialEntity.setDescription(special.getSpecialDescription() != null ? special.getSpecialDescription() : specialEntity.getDescription());
            
            specialDao.createOrUpdate(specialEntity);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return special;
    }
    
    public void deleteSpecial(Special special)
    {
        
        try {
            List<MenuSpecialPlacementEntity> specialPlacements = menuSpecialPlacementDao.queryForEq("SpecialId", special.getSpecialId());
            menuSpecialPlacementDao.delete(specialPlacements);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }  
    
    public List<Special> getSpecialsForPromotions() throws SQLException{
        List<Special> specials = new ArrayList<Special>();
        List<MenuEntity> currentActiveMenus = menuDao.queryForEq("Deleted", false);
        for(MenuEntity menu : currentActiveMenus){
            specials.addAll(getSpecialsForPortal(menu));
        }
        return specials;
    }
    
    public List<Special> getSpecialsForPortal(MenuEntity menuEntity){
        return getSpecials(menuEntity, false, null);
    }
    
    public List<Special> getSpecials(MenuEntity menuEntity, boolean checkValidityRange, Date futureOrderDate)
    {
        Date requestedDateTime = futureOrderDate == null ? new DateTime(DateTimeZone.UTC).toDate() :  futureOrderDate;                
        List<Special> specials = new ArrayList<Special>();        
        
        for(MenuSpecialPlacementEntity specialApplicability: menuEntity.getMenuspecialapplicabilities())
        {            
            SpecialEntity specialEntity  = specialApplicability.getSpecials();
            
            if(specialEntity != null)
            {                
                
                Special special = new Special(specialEntity.getId(), specialEntity.getSpecialName(), specialEntity.getDisplaySequence(), specialEntity.getDescription(), specialEntity.getItem().getId(), specialEntity.getPrice());                    
                special.setSpecialValidityDateRange(new Calendarization(new DateTime(specialEntity.getValidFrom()), new DateTime(specialEntity.getValidThrough())));    
                special.setSpecialValidityTimeRange(new ValidityTimeRange(new LocalTime(specialEntity.getValidTimeFrom()).plusMillis(utcOffSet).toString(), new LocalTime(specialEntity.getValidTimeThrough()).plusMillis(utcOffSet).toString()));
                special.setHighlighted(specialEntity.isHighlighted());
                Iterator<ItemOptionEntity> itemOptionIterator = specialEntity.getItem().getItemOptions().iterator();
                Iterator<EntreeCombinationEntity> itemCombinationIterator = specialEntity.getItem().getEntreeCombinations().iterator();
                
                if(itemOptionIterator.hasNext())
                    special.setMenuItemOptionList( new ArrayList<Option>());
                while(itemOptionIterator.hasNext())
                {                        
                    ItemOptionEntity optionEntity = itemOptionIterator.next();
                    special.getMenuItemOptionList().add(new Option(optionEntity.getOption().getId()));
                }
                
                if(itemCombinationIterator.hasNext())
                    special.setCombinationList( new ArrayList<MenuCombination>());
                while(itemCombinationIterator.hasNext())
                {                        
                    EntreeCombinationEntity entreeCombinationEntity = itemCombinationIterator.next();
                    special.getCombinationList().add(new MenuCombination(entreeCombinationEntity.getCombination().getId()));
                }
                specials.add(special);
            }
            
        }
        return specials;
    }
    
    public void deleteMenuCombination(int combinationId)
    {
        try {
            CombinationEntity combinationEntity  = combinationDao.queryForId(combinationId);
            if(combinationEntity == null || combinationEntity.isDeleted()) throw new ResourceNotFoundException("No Combination found for the given combinationId");
            combinationEntity.setDeleted(true);
            combinationDao.createOrUpdate(combinationEntity);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public List<MenuCombination> getMenuCombinations(int menuId)
    {
        try {
            MenuEntity menu = menuDao.queryForId(menuId);
            if(menu == null) throw new ResourceNotFoundException("No Menu found for the given MenuId");
            
            return getMenuCombinations(menu);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public MenuCombination getMenuCombination(int combinationId)
    {
        MenuCombination combination = null;
        try {
            CombinationEntity combinationEntity = combinationDao.queryForId(combinationId);
            if(combinationEntity == null || combinationEntity.isDeleted()) throw new ResourceNotFoundException("No Combination found for the given combinationId");
            
            combination = getMenuCombination(combinationEntity);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return combination;
    }
    
    private MenuCombination getMenuCombination(CombinationEntity combinationEntity)
    {
        MenuCombination combination = new MenuCombination(combinationEntity.getId(), combinationEntity.getName(), combinationEntity.getPricingDetail(), combinationEntity.getPricingType().getName());
        
        Iterator<CombinationElementEntity> combinationElementIterator = combinationEntity.getCombinationElements().iterator();
        if(combinationElementIterator.hasNext())
            combination.setCombinationElements(new ArrayList<CombinationElement>());
            
        while(combinationElementIterator.hasNext())
        {
            CombinationElementEntity combinationElementEntity = combinationElementIterator.next();
            CombinationElement element = 
                    new CombinationElement(combinationElementEntity.getId(), combinationElementEntity.getClassificationId(), combinationElementEntity.getClassification(), combinationElementEntity.getPosition());
            
            Iterator<CombinationItemEntity> combinationItemIterator = combinationElementEntity.getCombinationItems().iterator();
            if(combinationItemIterator.hasNext())
                element.setCombinationItems(new ArrayList<CombinationItem>());
            while(combinationItemIterator.hasNext())
            {
                CombinationItemEntity combinationItemEntity = combinationItemIterator.next();
                CombinationItem combinationItem = 
                        new CombinationItem(combinationItemEntity.getItem().getId(), combinationItemEntity.getPricingDetail());
                element.getCombinationItems().add(combinationItem);
            }
                
            combination.getCombinationElements().add(element);
        }
        
        return combination;
    }
    
    private List<MenuCombination> getMenuCombinations(MenuEntity menuEntity)
    {
        Iterator<CombinationEntity> menuCombinationsIterator = menuEntity.getCombinations().iterator();
        
        List<MenuCombination> combinations = new ArrayList<MenuCombination>();
        while(menuCombinationsIterator.hasNext())
        {
            CombinationEntity combinationEntity = menuCombinationsIterator.next();
            if(!combinationEntity.isDeleted())
            {
                MenuCombination combination = getMenuCombination(combinationEntity);
                combinations.add(combination);
            }    
        }
        return combinations;
    }
    
    public MenuCombination createMenuCombination(MenuCombination combination)
    {
        MenuEntity menuEntity;
        CombinationEntity combinationEntity;
        try {
            menuEntity = menuDao.queryForId(combination.getMenuId());
        
            if(menuEntity == null) throw new ResourceNotFoundException("No Menu found for the given menuId");
            CombinationPricingTypeEntity combinationPricingTypeEntity = combinationPricingTypeDao.
                    queryForFirst(combinationPricingTypeDao.queryBuilder().
                            where().eq("Name", combination.getPricingType()).prepare());
            if(combinationPricingTypeEntity == null) throw new IllegalArgumentException("Invalid Pricing Type");
            if((combinationPricingTypeEntity.getName() == "CustomizedTotal" || 
                combinationPricingTypeEntity.getName() == "SubTotalDiscounted") && combination.getPricingDetail() == BigDecimal.ZERO)
                throw new IllegalArgumentException("Pricing Detail must not be 0");
            
            combinationEntity = new CombinationEntity();
            combinationEntity.setMenu(menuEntity);
            combinationEntity.setName(combination.getName());
            combinationEntity.setPricingDetail(combination.getPricingDetail());
            combinationEntity.setPricingType(combinationPricingTypeEntity);
                        
            combinationDao.createOrUpdate(combinationEntity);
            for(CombinationElement combinationElement : combination.getCombinationElements() ){
                   CombinationElementEntity combinationElementEntity = 
                           new CombinationElementEntity(combinationElement.getClassificationId(), combinationElement.getClassification(), combinationElement.getPosition(), combinationEntity);
                   combinationElementDao.createOrUpdate(combinationElementEntity);
                   for(CombinationItem combinationItem : combinationElement.getCombinationItems())
                   {
                       CombinationItemEntity combinationItemEntity = 
                               new CombinationItemEntity(combinationItem.getPricingDetail(), new ItemEntity(combinationItem.getMenuItemId()), combinationElementEntity);
                       combinationItemDao.createOrUpdate(combinationItemEntity);
                   }
            }
            combinationDao.refresh(combinationEntity);
            combination = getMenuCombination(combinationEntity);            
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return combination;
    }
    
    public MenuCombination editMenuCombition(MenuCombination combination)
    {
        try {
            CombinationEntity combinationEntity = combinationDao.queryForId(combination.getCombinationId());
            if(combinationEntity == null) throw new ResourceNotFoundException("No Combination found for the given combinationId");
            
            CombinationPricingTypeEntity combinationPricingTypeEntity = combinationPricingTypeDao.
                    queryForFirst(combinationPricingTypeDao.queryBuilder().
                            where().eq("Name", combination.getPricingType()).prepare());
            if(combinationPricingTypeEntity == null) throw new IllegalArgumentException("Invalid Pricing Type");
            if((combinationPricingTypeEntity.getName() == "CustomizedTotal" || 
                combinationPricingTypeEntity.getName() == "SubTotalDiscounted") && combination.getPricingDetail() == BigDecimal.ZERO)
                throw new IllegalArgumentException("Pricing Detail must not be 0");
            
            MenuEntity menuEntity = menuDao.queryForId(combination.getMenuId());            
            if(menuEntity == null) throw new ResourceNotFoundException("No Menu found for the given menuId");
            
            combinationEntity.setDeleted(true);
            combinationDao.createOrUpdate(combinationEntity);
            
            CombinationEntity newCombination = new CombinationEntity(combination.getName(), combination.getPricingDetail(), combinationPricingTypeEntity, menuEntity , null);
            combinationDao.createOrUpdate(newCombination);
            
            for(CombinationElement combinationElement : combination.getCombinationElements() ){
                   CombinationElementEntity combinationElementEntity = 
                           new CombinationElementEntity(combinationElement.getClassificationId(), combinationElement.getClassification(), combinationElement.getPosition(), newCombination);
                   combinationElementDao.createOrUpdate(combinationElementEntity);
                   for(CombinationItem combinationItem : combinationElement.getCombinationItems())
                   {
                       CombinationItemEntity combinationItemEntity = 
                               new CombinationItemEntity(combinationItem.getPricingDetail(), new ItemEntity(combinationItem.getMenuItemId()), combinationElementEntity);
                       combinationItemDao.createOrUpdate(combinationItemEntity);
                   }
            }
            
            List<EntreeCombinationEntity> entreeCombinations = entreeCombinationDao.queryForEq("CombinationId", combinationEntity.getId());
            
            for(EntreeCombinationEntity entreeCombinationEntity : entreeCombinations)
            {
                entreeCombinationEntity.setCombination(newCombination);
                entreeCombinationDao.createOrUpdate(entreeCombinationEntity);
            }
            
            combinationDao.refresh(newCombination);
            combination = getMenuCombination(newCombination);
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return combination;
    }
    
    public List<Option> getMenuOptions(int menuId)
    {
        try {
            MenuEntity menu = menuDao.queryForId(menuId);
            if(menu == null) throw new ResourceNotFoundException("No Menu found for the given menuId");
            
            return getMenuOptions(menu);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    private Option getMenuOption(OptionEntity optionEntity)
    {
        Option menuOption = new Option(optionEntity.getId(), optionEntity.getName(), optionEntity.getDisplayName(), optionEntity.getDisplaySequence(), optionEntity.getValues(), optionEntity.getPrice());
        menuOption.setRequired(optionEntity.isRequired());
        menuOption.setDefaultSelected(optionEntity.isDefaultSelected());
        menuOption.setMultiQuantityAllowed(optionEntity.isMultiQuantityAllowed());
        menuOption.setMaxQuantity(optionEntity.getMaxQuantityAllowed());
        menuOption.setMinQuantity(optionEntity.getMinQuantityAllowed());
        menuOption.setOptionGroupId(optionEntity.getOptionGroup() != null ? optionEntity.getOptionGroup().getId() : 0);
        if(optionEntity.getOptionValues() != null){
            List<String> values = new ArrayList<String>(); //used for sending an comma separated string with values for supporting old approach
            List<OptionValue> optionValues = new ArrayList<OptionValue>();
            for(OptionValueEntity optionValueEntity: optionEntity.getOptionValues()){
                try {
                    optionValueDao.refresh(optionValueEntity);
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                values.add(optionValueEntity.getName());
                OptionValue optionValue = new OptionValue(optionValueEntity.getId(), optionValueEntity.getName(), optionValueEntity.getPrice());
                if(optionEntity.getDefaultOptionValue() != null && optionEntity.getDefaultOptionValue().getId() == optionValue.getId()){
                    optionValue.setDefaultValue(true);
                }
                optionValues.add(optionValue);
            }
            menuOption.setValues(StringUtils.join(values.toArray(), ","));
            menuOption.setOptionValues(optionValues);
        }
        return menuOption;
                
    }
    
    public Option getMenuOption(int optionId)
    {
        Option option = null;
        try {
            OptionEntity optionEntity = optionDao.queryForId(optionId);
            if(optionEntity == null || optionEntity.isDeleted()) throw new ResourceNotFoundException("No Option found for the given optionId");
            
            option = getMenuOption(optionEntity);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return option;
        
    }
    
    private List<Option> getMenuOptions(MenuEntity menuEntity)
    {
        Iterator<OptionEntity> menuOptionsIterator = menuEntity.getOptions().iterator();
        List<Option> options = new ArrayList<Option>();
        
        while(menuOptionsIterator.hasNext())
        {
            OptionEntity optionEntity = menuOptionsIterator.next();
            if(!optionEntity.isDeleted())
            {
                Option option = getMenuOption(optionEntity);
                options.add(option);
            }            
        }
                
        Collections.sort(options, new Comparator<Option>() {
            public int compare(Option o1, Option o2) {
                
                if(o1.getDisplaySequence().intValue() == o2.getDisplaySequence().intValue()) 
                    return o1.getOptionName().compareTo(o2.getOptionName());
                
                else if(o1.getDisplaySequence() > o2.getDisplaySequence())
                    return 1;
                
                return -1;
            }
        });
        
        return options;
    }
    
    public void deleteMenuOption(int optionId)
    {
        try {
            OptionEntity optionEntity = optionDao.queryForId(optionId);
            if(optionEntity == null || optionEntity.isDeleted()) throw new ResourceNotFoundException("No Option found for the given optionId");
            
            optionEntity.setDeleted(true);
            optionDao.createOrUpdate(optionEntity);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Transactional
    public Option editMenuOption(final Option option) throws SQLException
    {
            
            final OptionEntity  optionEntity = optionDao.queryForId(option.getId());
            if(optionEntity == null || optionEntity.isDeleted()) throw new ResourceNotFoundException("No Option found for the given optionId");
            
            OptionEntity editedOption = new OptionEntity(option.getOptionName().isEmpty() ? optionEntity.getName() : option.getOptionName(), 
                                                                option.getOptionDisplayName().isEmpty() ? optionEntity.getDisplayName() : option.getOptionDisplayName(),
                                                                option.getOptionPrice() == BigDecimal.ZERO ? optionEntity.getPrice() : option.getOptionPrice(),
                                                                option.getValues() == null ? optionEntity.getValues() : option.getValues(), 
                                                                optionEntity.getMenu());            
            editedOption.setDisplaySequence(option.getDisplaySequence());
            
            editedOption.setRequired(option.isRequired());
            editedOption.setDefaultSelected(option.isDefaultSelected());
            editedOption.setMultiQuantityAllowed(option.isMultiQuantityAllowed());
            if(option.isMultiQuantityAllowed()){
                editedOption.setMinQuantityAllowed(option.getMinQuantity());
                editedOption.setMaxQuantityAllowed(option.getMaxQuantity() < option.getMinQuantity() ? option.getMinQuantity() : option.getMaxQuantity());
            }  
            editedOption.setOptionGroup(option.getOptionGroupId() != 0 ? new OptionGroupEntity(option.getOptionGroupId()) : null);
            
            optionDao.create(editedOption);
            if(option.getOptionValues() != null){
                
                for(OptionValue optionValue: option.getOptionValues()){
                    OptionValueEntity optionValueEntity = new OptionValueEntity();
                    optionValueEntity.setName(optionValue.getName());
                    optionValueEntity.setPrice(optionValue.getPrice());
                    optionValueEntity.setOption(editedOption);
                    optionValueDao.createOrUpdate(optionValueEntity);
                    
                    if(optionValue.getDefaultValue() != null && optionValue.getDefaultValue() == true){
                        editedOption.setDefaultOptionValue(optionValueEntity);
                        optionDao.createOrUpdate(editedOption);
                    }
                }
            }
            
            optionEntity.setDeleted(true);
            
            optionDao.update(optionEntity);
            
            List<ItemOptionEntity> itemOptions = itemOptionDao.queryForEq("OptionId", optionEntity.getId());
            for(ItemOptionEntity itemOptionEntity : itemOptions)
            {
                itemOptionEntity.setOption(editedOption);
                itemOptionDao.update(itemOptionEntity);
            }
            
            option.setId(editedOption.getId());
        
        
        return option;
        
    }
    
    public OptionGroup createOptionGroup(OptionGroup optionGroup)
    {
        OptionGroupEntity optionGroupEntity = new OptionGroupEntity();
        try {
            optionGroupEntity.setName(optionGroup.getName());
            optionGroupDao.createOrUpdate(optionGroupEntity);            
            optionGroup.setId(optionGroupEntity.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return optionGroup;        
    }
    
    public OptionGroup editOptionGroup(OptionGroup optionGroup)
    {
        try {
            OptionGroupEntity optionGroupEntity = optionGroupDao.queryForId(optionGroup.getId());
            if(optionGroupEntity!= null){
                optionGroupEntity.setName(optionGroup.getName());
                optionGroupDao.createOrUpdate(optionGroupEntity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return optionGroup;        
    }
    
    @Transactional
    public void deleteOptionGroup(int optionGroupId)
    {
        try {
            OptionGroupEntity optionGroupEntity = optionGroupDao.queryForId(optionGroupId);
            if(optionGroupEntity!= null){
                if(optionGroupEntity.getOptions() != null){
                    for(OptionEntity optionEntity: optionGroupEntity.getOptions()){
                        optionEntity.setOptionGroup(null);
                        optionDao.update(optionEntity);
                    }
                }
                optionGroupDao.delete(optionGroupEntity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
             
    }
    
    public List<OptionGroup> getOptionGroups()
    {
        List<OptionGroup> optionGroups = new ArrayList<OptionGroup>();
        try {
            List<OptionGroupEntity> optionGroupEntities = optionGroupDao.queryForAll();
            for(OptionGroupEntity optionGroupEntity : optionGroupEntities){
                optionGroups.add(new OptionGroup(optionGroupEntity.getId(), optionGroupEntity.getName()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return optionGroups;        
    }
    @Transactional
    public Option createMenuOption(Option option)
    {
        MenuEntity menuEntity;
        try {
            menuEntity = menuDao.queryForId(option.getMenuId());
            if(menuEntity == null) throw new ResourceNotFoundException("No menu found for the given menuId");
            
            OptionEntity optionEntity = new OptionEntity(option.getOptionName(), option.getOptionDisplayName(), option.getOptionPrice(), option.getValues(), menuEntity);
            optionEntity.setDisplaySequence(option.getDisplaySequence());
            optionEntity.setRequired(option.isRequired());
            optionEntity.setDefaultSelected(option.isDefaultSelected());
            optionEntity.setMultiQuantityAllowed(option.isMultiQuantityAllowed());
            optionEntity.setMinQuantityAllowed(option.getMinQuantity());
            optionEntity.setMaxQuantityAllowed(option.getMaxQuantity() < option.getMinQuantity() ? option.getMinQuantity() : option.getMaxQuantity());
            optionEntity.setOptionGroup(option.getOptionGroupId() != 0 ? new OptionGroupEntity(option.getOptionGroupId()) : null);
            optionDao.createOrUpdate(optionEntity);
            
            if(option.getOptionValues() != null){
                
                for(OptionValue optionValue: option.getOptionValues()){
                    OptionValueEntity optionValueEntity = new OptionValueEntity();
                    optionValueEntity.setName(optionValue.getName());
                    optionValueEntity.setPrice(optionValue.getPrice());
                    optionValueEntity.setOption(optionEntity);
                    optionValueDao.createOrUpdate(optionValueEntity);
                    
                    if(optionValue.getDefaultValue() != null && optionValue.getDefaultValue() == true){
                        optionEntity.setDefaultOptionValue(optionValueEntity);
                        optionDao.createOrUpdate(optionEntity);
                    }
                }
            }
            
            
            option.setId(optionEntity.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return option;        
    }
    
    public List<MenuModel> getAllMenus()
    {
        List<MenuModel> menuModelList = new ArrayList<MenuModel>();
        
        try {
            List<MenuEntity> menuEntityList = menuDao.queryForEq("Deleted", false);
            Iterator<MenuEntity> menuEntityIterator = menuEntityList.iterator();
            while(menuEntityIterator.hasNext())
            {
                menuModelList.add(getMenu(null, 0, null, menuEntityIterator.next(), false));
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Collections.sort(menuModelList, new Comparator<MenuModel>() {
            public int compare(MenuModel m1, MenuModel m2) {
                
                if(m1.getDisplaySequence() == m2.getDisplaySequence()) 
                    return m1.getMenuName().compareTo(m2.getMenuName());
                
                else if(m1.getDisplaySequence() > m2.getDisplaySequence())
                    return 1;
                
                return -1;
            }
        });
        
        
        return menuModelList;
    }
    
    public MenuModel getMenu(int menuId)
    {
        MenuModel model = null;
        try {
                
            MenuEntity menuEntity = menuDao.queryForId(menuId);
            if(menuEntity == null || menuEntity.isDeleted()) throw new ResourceNotFoundException("No menu found for the given menuId");
            
            model = getMenu(null, 0, null,  menuEntity, false);
            
            menuDao.getConnectionSource().close();
        } catch (SQLException e) {
            
            e.printStackTrace();
        }
        
        
        return model;
    }
    
    public boolean deleteMenu(int menuId)
    {
        MenuEntity menuEntity;
        try {
            menuEntity = menuDao.queryForId(menuId);
            if(menuEntity == null) throw new ResourceNotFoundException("No Menu found for the given menuId");
            
            menuEntity.getSchedules().clear();
            
            ForeignCollection<MenuItemPlacementEntity> menuItemPlacements = menuEntity.getMenuitemplacements();
            
            for(MenuItemPlacementEntity itemPlacementEntity : menuItemPlacements)
            {
                ItemEntity itemEntity = itemPlacementEntity.getItem();
                itemEntity.setCategoryEntity(null);
                itemDao.update(itemEntity);
            }
            
            menuEntity.getSchedules().clear();
            
            menuItemPlacementDao.delete(menuItemPlacements);
            ForeignCollection<MenuSpecialPlacementEntity> menuSpecials= menuEntity.getMenuspecialapplicabilities();
            menuSpecialPlacementDao.delete(menuSpecials);
            ForeignCollection<OptionEntity> menuOptions= menuEntity.getOptions();
            
            for(OptionEntity option:  menuOptions)
            {
                option.setDeleted(true);
                optionDao.update(option);
            }
            
            ForeignCollection<CombinationEntity> menuCombinations= menuEntity.getCombinations();
            for(CombinationEntity combinationEntity: menuCombinations)
            {
                combinationEntity.setDeleted(true);
                combinationDao.update(combinationEntity);
            }
            
            menuEntity.setDeleted(true);
            menuDao.update(menuEntity);
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
        
        
    }
    
    public ScheduleEntity getSchedule(String criteria, int id ,int dayOfWeekId) throws SQLException
    {
        ScheduleEntity schedule = scheduleDao.queryForFirst(scheduleDao.queryBuilder().where().eq(criteria, id).and().eq("DayOfWeekId", dayOfWeekId).prepare());
        return schedule;
    }
    
    private Schedule getWeeklySchedule(String criteria, int id) throws SQLException
    {
        Schedule scheduleModel = new Schedule();
        List<ScheduleEntity> weeklySchedule = scheduleDao.query(scheduleDao.queryBuilder().where().eq(criteria, id).prepare());
        
        List<Integer> daysArray = new ArrayList<Integer>();//int[weeklySchedule.size()];
        for(int i = 0 ; i < weeklySchedule.size(); i++)
        {
            daysArray.add(weeklySchedule.get(i).getDayOfWeekEntity().getId());
        }
        scheduleModel.setDaysOfWeek(daysArray);
        return scheduleModel;
    }
    
    public boolean isMenuAvailable(MenuEntity menuEntity, int utcOffSet) throws SQLException
    {
        
        DateTime currentDate = new DateTime(DateTimeZone.UTC);
        int requestedDayOfWeekId = new DateTime(DateTimeZone.UTC).plusMillis(utcOffSet).getDayOfWeek();
        return isMenuAvailable(menuEntity, currentDate.toDate(), requestedDayOfWeekId);
    }
    
    public boolean isMenuAvailable(MenuEntity menuEntity, DateTime requestedDateTime, int utcOffSet) throws SQLException
    {            
        if(requestedDateTime == null){
            return isMenuAvailable(menuEntity, utcOffSet);
        }
        else{
            int requestedDayOfWeekId = requestedDateTime.plusMillis(utcOffSet).getDayOfWeek();
            return isMenuAvailable(menuEntity, requestedDateTime.toDate(), requestedDayOfWeekId);
        }
    }
    
    /**
     * Checks if a menu is available for a given day and time of the week
     * Ex: When requestedDateTime is 05:00AM and menu's ValidTimeFrom is 18:00 and ValidTimeThrough is 07:30, check if 
     * requestedDateTime is within range of the menu's valid times.
     * @param menuEntity Contains Date objects: ValidTimeFrom and ValidTimeThrough
     * @param requestedDateTime If null, then check is done against current UTC time.
     * @param dayOfWeekId The day of the week id, where Sunday is 1, Monday is 2, etc...
     * @return true if the menu is available, false otherwise
     * @throws SQLException
     */
    public boolean isMenuAvailable(MenuEntity menuEntity, Date requestedDateTime, int dayOfWeekId) throws SQLException
    {
        
        if(getSchedule("MenuId", menuEntity.getId(), dayOfWeekId) == null){
            return false;
        }
        
        LocalTime requestedUtcTime = requestedDateTime == null ? new LocalTime(DateTimeZone.UTC) : new DateTime(requestedDateTime, DateTimeZone.UTC).toLocalTime();    
        
        LocalTime startTime = LocalTime.parse(menuEntity.getValidTimeFrom());
        LocalTime endTime = LocalTime.parse(menuEntity.getValidTimeThrough());
        
        if(startTime.isAfter(endTime))
        {
            if(requestedUtcTime.isAfter(endTime) && requestedUtcTime.isBefore(startTime))
                return false;
        }
        else
        {
            if(requestedUtcTime.isBefore(startTime) || requestedUtcTime.isAfter(endTime))
                return false;
        }
        
        return true;
    }
    
    private MenuModel getMenu(Date futureOrderDate, int currentDayOfWeekId, List<Integer> currentCategories, MenuEntity menuEntity, boolean isApiCall) throws SQLException
    {
        if(utcOffSet == 0){
            utcOffSet = restaurantService.getCurrentUtcOffSet();
        }
        
        if(isApiCall && !isMenuAvailable(menuEntity, futureOrderDate, currentDayOfWeekId ))
        {
            return null;            
        }        
                        
        MenuModel model = new MenuModel(menuEntity.getId(), 
                menuEntity.getDisplaySequence(),
                menuEntity.getName(), 
                menuEntity.getStatus(), 
                new Calendarization(new DateTime(menuEntity.getValidFrom()), new DateTime(menuEntity.getValidThrough()), getWeeklySchedule("MenuId", menuEntity.getId())),
                new ValidityTimeRange(new LocalTime(menuEntity.getValidTimeFrom()).plusMillis(utcOffSet).toString(),
                        new LocalTime(menuEntity.getValidTimeThrough()).plusMillis(utcOffSet).toString())
        );
        
        model.setPromotionalTexts(getPromotionalTexts(menuEntity, isApiCall, utcOffSet));
        
        model.setMenuItems(getMenuItems(menuEntity, currentDayOfWeekId, isApiCall, currentCategories, futureOrderDate));    
                                                    
        if(!isApiCall){
            model.setSpecials(getSpecialsForPortal(menuEntity));
        }
        
        model.setMenuCombinations(getMenuCombinations(menuEntity));            
        
        model.setOptions(getMenuOptions(menuEntity));
        
        return model;
    }
    
    private List<Integer> getCurrentCategories(int currentDayOfWeekId) throws SQLException
    {
        List<CategoryEntity> categories = categoryDao.queryForAll();
        List<Integer> categoryIds = new ArrayList<Integer>();
        for(CategoryEntity categoryEntity : categories)
        {
            if(categoryEntity.isEnabled() && getSchedule("CategoryId", categoryEntity.getId(), currentDayOfWeekId) != null)
            {
                categoryIds.add(categoryEntity.getId());
            }
        }
        return categoryIds;        
    }
    
    public List<MenuModel> getMenus(Date futureOrderDate) throws SQLException
    {
        if(utcOffSet == 0){
            utcOffSet = restaurantService.getCurrentUtcOffSet();
        }       
        
        List<MenuModel> menusList = new ArrayList<MenuModel>();
        int currentDayOfWeekId = 0;
        if(futureOrderDate != null)
        {
            currentDayOfWeekId = new DateTime(futureOrderDate, DateTimeZone.UTC).plusMillis(utcOffSet).getDayOfWeek();
        }
        else
        {
            currentDayOfWeekId = new DateTime(DateTimeZone.UTC).plusMillis(utcOffSet).getDayOfWeek();
        }
        
        List<MenuEntity> menuDtoList = menuDao.queryForEq("Deleted", false);
        List<Integer> currentCategories = getCurrentCategories(currentDayOfWeekId);
        for(int i = 0 ; i < menuDtoList.size() ; i++)
        {
            MenuEntity menuEntity = menuDtoList.get(i);
            if(!menuEntity.getStatus().equalsIgnoreCase("active")) continue;
            
            MenuModel model = getMenu(futureOrderDate, currentDayOfWeekId, currentCategories, menuEntity, true);
            
            if(model != null)
                menusList.add(model);
        }
        
        menuDao.getConnectionSource().close();
        
        Collections.sort(menusList, new Comparator<MenuModel>() {
            public int compare(MenuModel m1, MenuModel m2) {
                
                if(m1.getDisplaySequence() == m2.getDisplaySequence()) 
                    return m1.getMenuName().compareTo(m2.getMenuName());
                
                else if(m1.getDisplaySequence() > m2.getDisplaySequence())
                    return 1;
                
                return -1;
            }
        });
        
        return menusList;
    }
    
    public MenuModel createMenu(MenuModel model)
    {
        try {
            ServerSettingEntity settings = serverSettingDao.queryForFirst(serverSettingDao.queryBuilder().prepare());
            DateTimeZone zone = DateTimeZone.forID(settings.getTimeZoneId());
            int utcOffSet = zone.getOffset(new DateTime(DateTimeZone.UTC).withTime(0, 0, 0,
                    0));
            
            MenuEntity menuEntity = new MenuEntity(model.getDisplaySequence(), model.getMenuName(), 
                    model.getMenuValidityDateRange().getStartDate().withZone(DateTimeZone.UTC).withTime(0, 0, 0, 0).plusMillis(utcOffSet*-1).toDate(), 
                    model.getMenuValidityDateRange().getEndDate().withZone(DateTimeZone.UTC).withTime(23,59,59, 0).plusMillis(utcOffSet*-1).toDate(), 
                    new LocalTime(model.getMenuValidityTimeRange().getStartTime()).plusMillis(utcOffSet *-1).toString(),
                    new LocalTime(model.getMenuValidityTimeRange().getEndTime()).plusMillis(utcOffSet *-1).toString(),
                    
                    model.getStatus());
            menuEntity.setMenuType(new MenuTypeEntity(1));
            
            
            menuDao.createIfNotExists(menuEntity);
            model.setMenuId(menuEntity.getId());
            
            for(Integer weekDayId: model.getMenuValidityDateRange().getSchedule().getDaysOfWeek())
            {
                ScheduleEntity schedule = new ScheduleEntity(menuEntity, null, null, new DaysOfWeekEntity(weekDayId));
                scheduleDao.createOrUpdate(schedule);
            }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return model;
    }
    
    public MenuModel editMenu(MenuModel model)
    {
        try {
            
            ServerSettingEntity restaurantSettings = serverSettingDao.queryForFirst(serverSettingDao.queryBuilder().prepare());
            DateTimeZone zone = DateTimeZone.forID(restaurantSettings.getTimeZoneId());
            int utcOffSet = zone.getOffset(new DateTime(DateTimeZone.UTC).withTime(0, 0, 0,
                    0));
            
            //LocalTime t = new LocalTime(model.getMenuValidityTimeRange().getStartTime()).plusMillis(utcOffSet).toString();
            MenuEntity menuEntity = menuDao.queryForId(model.getMenuId());
            if(menuEntity == null) throw new ResourceNotFoundException("No Menu found for the given menuId");
            menuEntity.setDisplaySequence(model.getDisplaySequence());
            menuEntity.setName(model.getMenuName());
            menuEntity.setValidFrom(model.getMenuValidityDateRange().getStartDate().toDate());
            menuEntity.setValidThrough(model.getMenuValidityDateRange().getEndDate().toDate());
            menuEntity.setValidTimeFrom(new LocalTime(model.getMenuValidityTimeRange().getStartTime()).plusMillis(utcOffSet*-1).toString());
            menuEntity.setValidTimeThrough(new LocalTime(model.getMenuValidityTimeRange().getEndTime()).plusMillis(utcOffSet*-1).toString());
            menuEntity.setStatus(model.getStatus());    
            menuEntity.getSchedules().clear();
            for(Integer weekDayId: model.getMenuValidityDateRange().getSchedule().getDaysOfWeek())
            {
                ScheduleEntity schedule = new ScheduleEntity(menuEntity, null, null, new DaysOfWeekEntity(weekDayId));
                scheduleDao.createOrUpdate(schedule);
            }
        
            menuDao.createOrUpdate(menuEntity);
            model.setMenuId(menuEntity.getId());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return model;
    }
    
    
    public RestaurantModel getRestaurantMenus(Date futureOrderDate)
    {
        RestaurantModel restaurantModel = null;        
        try {
            
            
            
            ServerSettingEntity restaurantSettings = serverSettingDao.queryForFirst(serverSettingDao.queryBuilder().prepare());
            TaxRateEntity currentTaxRate = restaurantService.getCurrentTaxRate();
            if(futureOrderDate != null)
            {
                DateTime futureDate = new DateTime(futureOrderDate);
                DateTime maximumAllowedOrderDate = new DateTime(DateTimeZone.UTC).plus(restaurantSettings.getMaximumOrderTime());
                if(futureDate.isAfter(maximumAllowedOrderDate)){
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy"); 
                    throw new MaximumOrderDateExceededException("This restaurant does not accept orders later than " + maximumAllowedOrderDate.toString(formatter));
                }
                
            }
            
            PeriodFormatter periodFormater = ISOPeriodFormat.alternateExtended();
            restaurantModel = new RestaurantModel(restaurantSettings.getName(), restaurantSettings.getOrganization(), restaurantSettings.getBrandName(),
    
                                                    restaurantService.getOperationalHoursPretty(null), 
                                                    restaurantSettings.getStreetAddress(), restaurantSettings.getCity(), restaurantSettings.getState(), 
                                                    restaurantSettings.getZipCode(), restaurantSettings.getLocation(), currentTaxRate.getTaxRate(), 
                                                    futureOrderDate == null ? restaurantService.isRestaurantOpen() : restaurantService.isRestaurantOpen(new DateTime(futureOrderDate, DateTimeZone.UTC)),
                                                    restaurantSettings.getCurrentStatus().equalsIgnoreCase("Enabled") ? true: false,
                                                    restaurantSettings.getDeliveryCapable(), 
                                                    restaurantSettings.getDeliveryDestination(),
                                                    restaurantSettings.getMinimumOrder(),
                                                    restaurantSettings.getDeliveryFee(),
                                                    restaurantSettings.getDeliveryPickupLabel(),
                                                    restaurantSettings.getDeliveryDeliveryLabel(),                                                    
                                                    periodFormater.print(new PeriodConverter(restaurantSettings.getSmallOrderMinimumLeadTime()).getPeriod()),
                                                    restaurantSettings.getSmallOrderAmount(),
                                                    periodFormater.print(new PeriodConverter(restaurantSettings.getLargeOrderMinimumLeadTime()).getPeriod()),
                                                    restaurantSettings.getLargeOrderAmount(),
                                                    periodFormater.print(new PeriodConverter(restaurantSettings.getMaximumOrderTime()).getPeriod()));
            if(dynamicBrandingEnabled) {
                //restaurantModel.setDynamicBranding(new DynamicBranding(organizationColor, locationDominantColor, coverPictureHigh, coverPictureMedium, coverPictureSmall, coverPictureTiny, logoPictureHigh, logoPictureMedium, logoPictureSmall, logoPictureTiny, mainMenuUsual, mainMenuFavorites, mainMenuPastOrders, mainMenuPlaceOrder, mainMenuOperationHours));
                restaurantModel.setDynamicBranding(new DynamicBranding(organizationColor, locationDominantColor, categoryDividerColor, titleStripColor, titleStripTextColor, backgroundHomeScreenColor, coverPictureExtraExtraHigh, coverPictureExtraHigh, coverPictureHigh, coverPictureMedium, coverPictureSmall, logoPictureExtraExtraHigh, logoPictureExtraHigh, logoPictureHigh, logoPictureMedium, logoPictureSmall, mainMenuUsual, mainMenuFavorites, mainMenuPastOrders, mainMenuPlaceOrder, mainMenuOperationHours));
            }            
            restaurantModel.setTaxRates(getTaxRatesByZipcode(restaurantSettings.getZipCode()));
            restaurantModel.setDeliveryRates(getDeliveryRatesByZipcode(restaurantSettings.getZipCode()));
            if(restaurantSettings.isVoiceEnabled()){                
                if(defaultCustomItemId == 0  || defaultSpecialtyMenuId == 0){
                    log.error("Voice is enabled but either voiceOrdering.defaultSpecialtyMenuId or voiceOrdering.defaultCustomItemId are not configured.");
                }else{
                    restaurantModel.setVoiceEnabled(restaurantSettings.isVoiceEnabled());
                    restaurantModel.setVoiceOrdering( new VoiceOrdering(defaultSpecialtyMenuId, defaultCustomItemId));
                }
                
            }
            
            restaurantModel.setIcon(restaurantService.getIcon());
            restaurantModel.setContactInformation(new 
                    ContactInformation(restaurantSettings.getTelephone(), restaurantSettings.getEmail(), restaurantSettings.getWebsite(), restaurantSettings.getFacebook()));
            restaurantModel.setMenus(getMenus(futureOrderDate));
            TippingSettings tippingSettings = restaurantSettings.getTippingEnabled() == true? new TippingSettings(true, restaurantSettings.getTipPercentage()) : new TippingSettings(false);
            tippingSettings.setDeliveryAutopopulate(restaurantSettings.getDeliveryAutoPopulateTip());
            tippingSettings.setPickupAutopopulate(restaurantSettings.getPickupAutoPopulateTip());
            tippingSettings.setDeliveryMinimumTipPercentage(restaurantSettings.getDeliveryMinimumTipPercentage());
            tippingSettings.setPickupMinimumTipPercentage(restaurantSettings.getPickupMinimumTipPercentage());
            
            restaurantModel.setTippingSettings(tippingSettings);
                        
            TransactionOrderEntity transactionOrderEntity = new TransactionOrderEntity(merchantLocationItem + new BigInteger(50, randomGenerator).toString(32).toUpperCase(),  new BigInteger(50, randomGenerator).toString(32).toUpperCase() );
            transactionOrderDao.createOrUpdate(transactionOrderEntity);
              
            restaurantModel.setTransactionId(transactionOrderEntity.getTransactionId());
            restaurantModel.setDeliveryCapable(restaurantSettings.getDeliveryCapable());
            restaurantModel.setDeliveryDestination(restaurantSettings.getDeliveryDestination());
            
            restaurantModel.setPaymentTypes(restaurantService.getPaymentTypes());
            restaurantModel.setOptionGroups(getOptionGroups());
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            serverSettingDao.getConnectionSource().closeQuietly();
            if(serverSettingDao.getConnectionSource().isOpen())
            {
                
            }
        }
        return restaurantModel;
        
    }
    
    public List<Rate> getAllRatesByZipcode(String zipcode) throws SQLException{
        List<Rate> rates = new ArrayList<Rate>();
        List<TaxRate> taxRates = getTaxRatesByZipcode(zipcode);
        for(TaxRate taxRate: taxRates){
            rates.add(taxRate);
        }
        List<DeliveryRate> deliveryRates = getDeliveryRatesByZipcode(zipcode);
        for(DeliveryRate deliveryRate: deliveryRates){
            rates.add(deliveryRate);
        }
        return rates;
    }
    
    private List<ZipcodeRange> getGeofenceZipcodes(String zipcode) throws SQLException{
        if(zipcode.length() == 5){
            zipcode = zipcode + "-0000%";
        }
        
        else if(zipcode.length() == 10){
            zipcode = zipcode + "%";
        }
        
        List<ZipcodeRange> zipcodeRanges = new ArrayList<ZipcodeRange>();
        
        Where<GeoFenceZipcodeEntity, Integer> whereQuery = geofenceZipcodeDao.queryBuilder().where();
        @SuppressWarnings("unchecked")
        PreparedQuery<GeoFenceZipcodeEntity> pq1 = whereQuery.or(whereQuery.eq("Deleted", false).and().like("StartZipcode", zipcode), 
                whereQuery.eq("Deleted", false).and().like("EndZipcode", zipcode)).prepare();
        List<GeoFenceZipcodeEntity> taxZipCodeEntities =    geofenceZipcodeDao.query(pq1);
        
        if(taxZipCodeEntities.isEmpty()){ //If it's empty, then search to see if the given zipcode is within a given range
            taxZipCodeEntities = geofenceZipcodeDao.queryForEq("Deleted", false);
            String[] requestedZipcode = zipcode.replace("%", "").split("-");
            int requested5Digit = Integer.parseInt(requestedZipcode[0]);
            int requested4Digit = Integer.parseInt(requestedZipcode[1]);
            for(GeoFenceZipcodeEntity taxZipcodeEntity : taxZipCodeEntities){
                String[] startZip = taxZipcodeEntity.getStartZipcode().split("-");
                String[] endZip = taxZipcodeEntity.getEndZipcode().split("-");
                
                int start5Digit = Integer.parseInt(startZip[0]);
                int start4Digit = Integer.parseInt(startZip[1]);
                
                int end5Digit = Integer.parseInt(endZip[0]);
                int end4Digit = Integer.parseInt(endZip[1]);
                
                if((requested5Digit >= start5Digit  && requested5Digit <= end5Digit) && (requested4Digit >= start4Digit  && requested4Digit <= end4Digit)){
                    zipcodeRanges.add(new ZipcodeRange(taxZipcodeEntity.getId(),taxZipcodeEntity.getGeofenceAreaEntity().getId()));
                }
                
                        
            }
        }
        else{
            for(GeoFenceZipcodeEntity taxZipcodeEntity : taxZipCodeEntities){
                zipcodeRanges.add(new ZipcodeRange(taxZipcodeEntity.getId(),taxZipcodeEntity.getGeofenceAreaEntity().getId()));
            }
        }
        
        
        if(zipcodeRanges.isEmpty()){
            throw new ZipCodeOutOfRangeException(String.format("Zipcode %s is not within any Zipcode Range", zipcode));
        }
        return zipcodeRanges;
    }
    
    public List<DeliveryRate> getDeliveryRatesByZipcode(String zipcode) throws SQLException{
        List<ZipcodeRange> zipcodeRanges = getGeofenceZipcodes(zipcode);
        List<DeliveryRate> deliveryRates = new ArrayList<DeliveryRate>();
        
        for(ZipcodeRange zipcodeRange : zipcodeRanges){
            List<DeliveryRateEntity> deliveryRateEntities  = deliveryRateDao.query(deliveryRateDao.queryBuilder().where().eq("GeoFenceAreaId", new GeofenceAreaEntity(zipcodeRange.getGeofenceAreaId())).and().eq("Deleted", false).prepare());
            for(DeliveryRateEntity deliveryRateEntity: deliveryRateEntities){
                DeliveryRate deliveryRate = new DeliveryRate(deliveryRateEntity.getId(), deliveryRateEntity.getGeofenceAreaEntity().getId(), deliveryRateEntity.getGeofenceAreaEntity().getName(), deliveryRateEntity.getFixedDeliveryFee(), deliveryRateEntity.getMealFactorDeliveryFee());
                if(!deliveryRates.contains(deliveryRate)){
                    deliveryRates.add(deliveryRate);
                }
            }
        }
        
        return deliveryRates;
        
    }
    
    public DeliveryRate getDeliveryRateByZipcode(String zipcode) throws SQLException{
        List<ZipcodeRange> zipcodeRanges = getGeofenceZipcodes(zipcode);
        for(ZipcodeRange zipcodeRange: zipcodeRanges){
        	DeliveryRateEntity deliveryRateEntity = deliveryRateDao.queryForFirst(deliveryRateDao.queryBuilder().where().eq("GeoFenceAreaId", new GeofenceAreaEntity(zipcodeRange.getGeofenceAreaId())).and().eq("Deleted", false).prepare());//queryForFirst("GeoFenceAreaId", new GeofenceAreaEntity(zipcodeRange.getGeofenceAreaId()));
            if(deliveryRateEntity != null){
            	DeliveryRate deliveryRate = new DeliveryRate(deliveryRateEntity.getId(), deliveryRateEntity.getGeofenceAreaEntity().getId(), deliveryRateEntity.getGeofenceAreaEntity().getName(), deliveryRateEntity.getFixedDeliveryFee(), deliveryRateEntity.getMealFactorDeliveryFee());
                return deliveryRate;
            }
        }
        return null;
        
    }
    
    public List<TaxRate> getTaxRatesByZipcode(String zipcode) throws SQLException{
              
        List<ZipcodeRange> zipcodeRanges = getGeofenceZipcodes(zipcode);
        List<TaxRate> taxRates = new ArrayList<TaxRate>();       
        
        for(ZipcodeRange zipcodeRange : zipcodeRanges){
            List<TaxRateEntity> taxRateEntities  = taxRateDao.query(taxRateDao.queryBuilder().where().eq("GeoFenceAreaId", new GeofenceAreaEntity(zipcodeRange.getGeofenceAreaId())).and().eq("Deleted", false).prepare());
            for(TaxRateEntity taxRateEntity: taxRateEntities){
                TaxRate taxRate = new TaxRate(taxRateEntity.getId(), taxRateEntity.getTaxCategoryEntity().getId(), taxRateEntity.getGeofenceAreaId().getId(), taxRateEntity.getGeofenceAreaId().getName(), taxRateEntity.getTaxRate());
                if(!taxRates.contains(taxRate)){
                    taxRates.add(taxRate);
                }
            }
        }
        
        if(taxRates.isEmpty()){
        	throw new MissingTaxRateException(String.format("Zipcode %s has no tax rate associated.", zipcode));
        }
        
        return taxRates;
    }
    
    public List<PromotionalText> getPromotionalTexts(int menuId)
    {
        List<PromotionalText> promoTexts = new ArrayList<PromotionalText>();
        
        try {
            MenuEntity menuEntity = menuDao.queryForId(menuId);
            if(menuEntity == null) throw new ResourceNotFoundException("Invalid menuId");
            
            promoTexts = getPromotionalTexts(menuEntity, false, 0);
            if(promoTexts.size() == 0) throw new ResourceNotFoundException("No promotional texts found for the given menu.");
            
            } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return promoTexts;
    }
    
    public List<PromotionalText> getAllPromotionalTexts(boolean highlighted) throws SQLException{
        List<PromotionalText> promoTexts = new ArrayList<PromotionalText>();
        List<PromotionalTextEntity> promotionalTextsEntities = promotionalTextDao.queryForEq("Highlighted", highlighted);
        
        for(PromotionalTextEntity promoTextEntity: promotionalTextsEntities){
            promoTexts.add(new PromotionalText(promoTextEntity.getText(), promoTextEntity.getMenu().getId(), promoTextEntity.getShortName(), promoTextEntity.isHighlighted()));
        }
        return promoTexts;
    }
    
    public List<PromotionalText> getPromotionalTexts(MenuEntity menuEntity, boolean isApiCall, int utcOffSet)
    {
        List<PromotionalText> promoTexts = new ArrayList<PromotionalText>();
        DateTime currentUtcDateTime = new DateTime(DateTimeZone.UTC);
        
        int currentDayOfWeekId = isApiCall ? currentUtcDateTime.plusMillis(utcOffSet).getDayOfWeek() : 0;
        //try {
            Iterator<PromotionalTextEntity> promoTextEntities = menuEntity.getPromotionalTexts().iterator();
            while(promoTextEntities.hasNext())
            {
                PromotionalTextEntity promoTextEntity = promoTextEntities.next();
                
                try {
                    if (isApiCall && ((promoTextEntity.getValidThrough() != null && 
                            promoTextEntity.getValidFrom().compareTo(currentUtcDateTime.toDate()) * 
                            currentUtcDateTime.toDate().compareTo(promoTextEntity.getValidThrough()) <= 0) ||
                            getSchedule("PromotionalTextId", promoTextEntity.getId(), currentDayOfWeekId) == null ))
                        continue;
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }    
                
                PromotionalText promoText = getPromotionalText(0, promoTextEntity);
                promoTexts.add(promoText);
            }
        /*} catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        return promoTexts;
    }
    
    public PromotionalText getPromotionalText(int promoTextId, PromotionalTextEntity entity)
    {
        PromotionalText promoText = null;
        
        try {
            PromotionalTextEntity promoTextEntity = entity != null ? entity : promotionalTextDao.queryForId(promoTextId);    
            if(promoTextEntity == null) throw new ResourceNotFoundException("Cannot find the promotion.");
            
            promoText = new PromotionalText(promoTextEntity.getId(), 
                    promoTextEntity.getText(), 
                    promoTextEntity.getShortName(),
                    promoTextEntity.isHighlighted(),
                    promoTextEntity.getMenu().getId(), 
                    new Calendarization(promoTextEntity.getValidFrom() == null ? null : new DateTime(promoTextEntity.getValidFrom()), 
                            promoTextEntity.getValidThrough() == null ? null : new DateTime(promoTextEntity.getValidThrough()),
                                    getWeeklySchedule("PromotionalTextId", promoTextEntity.getId())));        
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return promoText;
    }
    
    public PromotionalText createOrUpdatePromotionalText(PromotionalText promoText)
    {
        try {
            MenuEntity menuEntity = menuDao.queryForId(promoText.getMenuId());
            if(menuEntity == null) throw new ResourceNotFoundException("Invalid menuId");
            
            ServerSettingEntity restaurantSettings = serverSettingDao.queryForFirst(serverSettingDao.queryBuilder().prepare());
            DateTimeZone zone = DateTimeZone.forID(restaurantSettings.getTimeZoneId());
            int utcOffSet = zone.getOffset(new DateTime(DateTimeZone.UTC).withTime(0, 0, 0,
                    0));
            
            PromotionalTextEntity promoTextEntity = new PromotionalTextEntity(promoText.getId(), promoText.getText(), new MenuEntity(promoText.getMenuId()), promoText.getCalendarization().getStartDate().withZone(DateTimeZone.UTC).withTime(0, 0, 0, 0).plus(utcOffSet*-1).toDate(), promoText.getCalendarization().getEndDate() == null ? null : promoText.getCalendarization().getEndDate().withZone(DateTimeZone.UTC).withTime(23, 59, 59, 0).plus(utcOffSet*-1).toDate());
            promoTextEntity.setHighlighted(promoText.getHighlighted());
            promoTextEntity.setShortName(promoText.getShortName());
            promotionalTextDao.createOrUpdate(promoTextEntity);    
            
            promotionalTextDao.refresh(promoTextEntity);
            Schedule schedule = promoText.getCalendarization().getSchedule();
            if(promoTextEntity.getSchedules() != null) 
                promoTextEntity.getSchedules().clear();
            
            for(int dayOfWeekId : schedule.getDaysOfWeek())
            {
                ScheduleEntity scheduleEntity = new ScheduleEntity(promoTextEntity, new DaysOfWeekEntity(dayOfWeekId));
                scheduleDao.createOrUpdate(scheduleEntity);
            }
            
            promoText.setId(promoTextEntity.getId());    
            iccService.sendSpecials(restaurantService.getIccSpecials());
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return promoText;
    }
    
    public int deletePromotionalText(int promoTextId)
    {
        
        try {
            return promotionalTextDao.deleteById(promoTextId);                
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
	}

	@Override
	public Object testService() {
		serviceName = "MenuService";
		return getRestaurantMenus(null);
    }
}

