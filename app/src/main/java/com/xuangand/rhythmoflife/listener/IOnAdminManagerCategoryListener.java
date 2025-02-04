package com.xuangand.rhythmoflife.listener;

import com.xuangand.rhythmoflife.model.Category;

public interface IOnAdminManagerCategoryListener {
    void onClickUpdateCategory(Category category);
    void onClickDeleteCategory(Category category);
    void onClickDetailCategory(Category category);
}
