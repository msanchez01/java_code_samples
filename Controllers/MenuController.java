package com.spfsolutions.ioms.controllers;


import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spfsolutions.ioms.annotations.LogRequest;
import com.spfsolutions.ioms.models.ImportedFile;
import com.spfsolutions.ioms.models.MenuModel;
import com.spfsolutions.ioms.models.RestaurantModel;
import com.spfsolutions.ioms.objects.Category;
import com.spfsolutions.ioms.objects.Classification;
import com.spfsolutions.ioms.objects.MenuCombination;
import com.spfsolutions.ioms.objects.MenuItem;
import com.spfsolutions.ioms.objects.Option;
import com.spfsolutions.ioms.objects.OptionGroup;
import com.spfsolutions.ioms.objects.PromotionalText;
import com.spfsolutions.ioms.objects.Rate;
import com.spfsolutions.ioms.objects.Special;
import com.spfsolutions.ioms.services.MenuService;
import com.spfsolutions.ioms.validators.FileValidator;

@Controller
public class MenuController {
    
    private static final Logger logger = Logger.getLogger(MenuController.class);
    ObjectMapper objMapper = new ObjectMapper();
    
    @Autowired  
     FileValidator fileValidator; 
    
    @Autowired
    MenuService menuService;
    
    @RequestMapping(value = "/menu", method = RequestMethod.GET)
    public String Index()
    {
        return "menu";
    }
            
    @RequestMapping(value = "/api/menu/", method = RequestMethod.GET)
    @LogRequest
    public @ResponseBody RestaurantModel getMenus(@RequestParam(value="futureOrderDate", required=false) @DateTimeFormat(pattern="MM/dd/yyyy HH:mm:ss") Date futureOrderDate)
    {
        return menuService.getRestaurantMenus(futureOrderDate);  
    }    
    
    @RequestMapping(value = "/api/menu/items/", method = RequestMethod.GET)
    @LogRequest
    public @ResponseBody List<MenuItem> getMenuItems(@RequestParam(value="keyword", required=true) String keyword, @RequestParam(value="futureDate", required=false) @DateTimeFormat(pattern="MM/dd/yyyy HH:mm:ss") Date futureDate) throws SQLException
    {
        return menuService.searchMenuItems(keyword, futureDate);  
    }   
    
    @RequestMapping(value = "/api/menu/taxzipcodes/", method = RequestMethod.GET)
    @LogRequest
    public @ResponseBody List<Rate> getTaxZipcodes(@RequestParam(value="zipcode", required=true) String zipcode) throws SQLException
    {
        return menuService.getAllRatesByZipcode(zipcode);    
    }    
    
    @RequestMapping(value = "/menu/all/", method = RequestMethod.GET)
    public @ResponseBody List<MenuModel> getAllMenus()
    {        
        return menuService.getAllMenus();        
    }
    
    @RequestMapping(value = "/menu/{menuId}/", method = RequestMethod.GET)
    public @ResponseBody MenuModel getMenu(@PathVariable int menuId)
    {
        return menuService.getMenu(menuId);        
    }
    
    @RequestMapping(value = "/menu/{menuId}/", method = RequestMethod.DELETE)
    public @ResponseBody boolean deleteMenu(@PathVariable int menuId)
    {
        return menuService.deleteMenu(menuId);        
    }
    
    @RequestMapping(value = "/menu/", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED) 
    public @ResponseBody MenuModel createMenu(@Valid @RequestBody MenuModel menuModel)
    {
        if(logger.isDebugEnabled()){
            logger.debug(menuModel);
        }
        return menuService.createMenu(menuModel);        
    }
    
    @RequestMapping(value = "/menu/", method = RequestMethod.PUT)
    public @ResponseBody MenuModel editMenu(@Valid @RequestBody MenuModel menuModel)
    {
        return menuService.editMenu(menuModel);        
    }
    
    @RequestMapping(value = "/menu/item/{id}", method = RequestMethod.GET)
    public @ResponseBody MenuItem getMenuItem(@PathVariable int id)
    {
        return menuService.getMenuItem(id);        
    }
    
    @RequestMapping(value = "/menu/item/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void deleteMenuItem(@PathVariable int id)
    {
        menuService.deleteMenuItem(id);        
    }
    
    @RequestMapping(value = "/menu/item/status/", method = RequestMethod.PUT)
    public @ResponseBody MenuItem changeMenuItemStatus(@RequestBody MenuItem menuItem) throws SQLException
    {
        return menuService.changeMenuItemStatus(menuItem);        
    }
    
    @RequestMapping(value = "/menu/item/", method = RequestMethod.PUT)
    public @ResponseBody MenuItem editMenuItem(@RequestBody MenuItem menuItem)
    {
        return menuService.editMenuItem(menuItem);        
    }
    
    @RequestMapping(value = "/menu/items/", method = RequestMethod.PUT)
    public @ResponseBody List<MenuItem> editMenuItem(@Valid @RequestBody List<MenuItem> menuItems)
    {
        return menuService.editMenuItems(menuItems);        
    }
    
    
    @RequestMapping(value = "/menu/item/", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED) 
    public @ResponseBody MenuItem addMenuItem(@Valid @RequestBody MenuItem menuItem)
    {
        return menuService.addMenuItem(menuItem);        
    }
    
    @RequestMapping(value = "/menu/classification/", method = RequestMethod.GET)
    public @ResponseBody List<Classification> getClassifications()
    {
        return menuService.getClassifications();
    }
    
    @RequestMapping(value = "/menu/classification/{classificationId}/", method = RequestMethod.GET)
    public @ResponseBody Classification getClassification(@PathVariable int classificationId)
    {
        return menuService.getClassification(classificationId);
    }
    
    @RequestMapping(value = "/menu/category/", method = RequestMethod.GET)
    public @ResponseBody List<Category> getCategories()
    {
        return menuService.getCategories();
    }
    
    @RequestMapping(value = "/menu/category/{categoryId}/", method = RequestMethod.GET)
    public @ResponseBody Category getCategory(@PathVariable int categoryId)
    {
        return menuService.getCategory(categoryId);
    }
    
    @RequestMapping(value = "/menu/category/{categoryId}/", method = RequestMethod.DELETE)
    public @ResponseBody Category deleteCategory(@PathVariable int categoryId)
    {
        return menuService.deleteCategory(categoryId);
    }
    
    @RequestMapping(value = "/menu/category/{categoryId}/items", method = RequestMethod.GET)
    public @ResponseBody List<MenuItem> getItemsPerCategory(@PathVariable int categoryId)
    {
        return menuService.getItemsPerCategory(categoryId);
    }
    
    @RequestMapping(value = "/menu/category/", method = RequestMethod.PUT)
    public @ResponseBody Category editCategory(@Valid @RequestBody Category categoryModel)
    {
        return menuService.editCategory(categoryModel);
    }
    
    @RequestMapping(value = "/menu/category/", method = RequestMethod.POST)    
    @ResponseStatus(HttpStatus.CREATED) 
    public @ResponseBody Category addCategory(@Valid @RequestBody Category categoryModel)
    {
        return menuService.addCategory(categoryModel);
    }
    
    @RequestMapping(value = "/menu/special/", method = RequestMethod.GET)
    public @ResponseBody List<Special> getSpecials(@RequestParam(value="menuId", required=true) int menuId)
    {
        return menuService.getSpecials(menuId);
    }
    
    @RequestMapping(value = "/menu/special/", method = RequestMethod.POST)
    public @ResponseBody Special addSpecial(@Valid @RequestBody Special special)
    {
        return menuService.createSpecial(special);
    }
    
    @RequestMapping(value = "/menu/special/{specialId}/", method = RequestMethod.DELETE)
    public @ResponseBody void deleteSpecial(@PathVariable int specialId)
    {
        menuService.deleteSpecial(specialId);
    }
    
    @RequestMapping(value = "/menu/special/", method = RequestMethod.PUT)
    public @ResponseBody Special updateSpecial(@Valid @RequestBody Special special)
    {
        return menuService.editSpecial(special);
    }
    
    @RequestMapping(value = "/menu/special/{specialId}/", method = RequestMethod.GET)
    public @ResponseBody Special getSpecial(@PathVariable int specialId)
    {
        return menuService.getSpecial(specialId);
    }
    
    @RequestMapping(value = "/menu/combination/{combinationId}/", method = RequestMethod.GET)
    public @ResponseBody MenuCombination getCombination(@PathVariable int combinationId)
    {
        return menuService.getMenuCombination(combinationId);
    }
    
    @RequestMapping(value = "/menu/combination/", method = RequestMethod.GET)
    public @ResponseBody List<MenuCombination> getCombinations(@RequestParam(value="menuId", required=true) int menuId)
    {
        return menuService.getMenuCombinations(menuId);
    }
    
    @RequestMapping(value = "/menu/combination/{combinationId}/", method = RequestMethod.DELETE)
    public @ResponseBody void deleteCombination(@PathVariable int combinationId)
    {
        menuService.deleteMenuCombination(combinationId);//menuService.createSpecial(special);
    }
    
    @RequestMapping(value = "/menu/combination/", method = RequestMethod.POST)    
    @ResponseStatus(HttpStatus.CREATED) 
    public @ResponseBody MenuCombination addCombination(@Valid @RequestBody MenuCombination combination)
    {
        return menuService.createMenuCombination(combination);
    }
    
    @RequestMapping(value = "/menu/combination/", method = RequestMethod.PUT)    
    @ResponseStatus(HttpStatus.ACCEPTED) 
    public @ResponseBody MenuCombination editCombination(@RequestBody MenuCombination combination)
    {
        return menuService.editMenuCombition(combination);
    }
    
    @RequestMapping(value = "/menu/option/{optionId}/", method = RequestMethod.GET)
    public @ResponseBody Option getOption(@PathVariable int optionId)
    {
        return menuService.getMenuOption(optionId);
    }

    @RequestMapping(value = "/menu/option/", method = RequestMethod.POST)    
    @ResponseStatus(HttpStatus.CREATED) 
    public @ResponseBody Option addOption(@Valid @RequestBody Option option)
    {
        return menuService.createMenuOption(option);
    }
        
    @RequestMapping(value = "/menu/option/", method = RequestMethod.GET)
    public @ResponseBody List<Option> getOptions(@RequestParam(value="menuId", required=true) int menuId)
    {
        return menuService.getMenuOptions(menuId);
    }
    
    @RequestMapping(value = "/menu/option/{optionId}/", method = RequestMethod.DELETE)
    public @ResponseBody void deleteOption(@PathVariable int optionId)
    {
        menuService.deleteMenuOption(optionId);//menuService.createSpecial(special);
    }
    
    @RequestMapping(value = "/menu/option/group/", method = RequestMethod.POST)    
    @ResponseStatus(HttpStatus.CREATED) 
    public @ResponseBody OptionGroup addOptionGroup(@Valid @RequestBody OptionGroup optionGroup)
    {
        return menuService.createOptionGroup(optionGroup);
    }
    
    @RequestMapping(value = "/menu/option/group/", method = RequestMethod.PUT)    
    @ResponseStatus(HttpStatus.ACCEPTED) 
    public @ResponseBody OptionGroup editOptionGroup(@Valid @RequestBody OptionGroup optionGroup)
    {
        return menuService.editOptionGroup(optionGroup);
    }
    
    @RequestMapping(value = "/menu/option/group/{optionGroupId}/", method = RequestMethod.DELETE)
    public @ResponseBody void deleteOptionGroup(@PathVariable int optionGroupId)
    {
        menuService.deleteOptionGroup(optionGroupId);
    }
    
    @RequestMapping(value = "/menu/option/group/", method = RequestMethod.GET)    
    @ResponseStatus(HttpStatus.CREATED) 
    public @ResponseBody List<OptionGroup> getOptionGroups()
    {
        return menuService.getOptionGroups();
    }
    
    @RequestMapping(value = "/menu/option/", method = RequestMethod.PUT)    
    @ResponseStatus(HttpStatus.ACCEPTED) 
    public @ResponseBody Option editOption(@Valid @RequestBody Option option) throws SQLException
    {
        return menuService.editMenuOption(option);
    }
    
    @RequestMapping(value = "/menu/promotionaltext/", method = RequestMethod.GET)
    public @ResponseBody List<PromotionalText> getPromotionalTexts(@RequestParam(value="menuId", required=true) int menuId)
    {
        return menuService.getPromotionalTexts(menuId);
    }
    
    @RequestMapping(value = "/menu/promotionaltext/{promotionalTextId}/", method = RequestMethod.GET)
    public @ResponseBody PromotionalText getPromotionalText(@PathVariable int promotionalTextId)
    {
        return menuService.getPromotionalText(promotionalTextId, null);
    }
    
    @RequestMapping(value = "/menu/promotionaltext/", method = RequestMethod.POST)    
    @ResponseStatus(HttpStatus.CREATED) 
    public @ResponseBody PromotionalText createPromotionalText(@Valid @RequestBody PromotionalText promotionaText)
    {
        return menuService.createOrUpdatePromotionalText(promotionaText);
    }
    
    @RequestMapping(value = "/menu/promotionaltext/", method = RequestMethod.PUT)    
    @ResponseStatus(HttpStatus.ACCEPTED) 
    public @ResponseBody PromotionalText editPromotionalText(@Valid @RequestBody PromotionalText promotionaText)
    {
        return menuService.createOrUpdatePromotionalText(promotionaText);
    }
    
    @RequestMapping(value = "/menu/promotionaltext/{promotionalTextId}/", method = RequestMethod.DELETE)
    public @ResponseBody void deletePromotionaText(@PathVariable int promotionalTextId)
    {
        menuService.deletePromotionalText(promotionalTextId);
    }
    
    
}

