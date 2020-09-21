/*Visibility Functions*/
export function setDefaultVisibility(){
    modifyElementVisibility("newCategoryButton","show");
    modifyElementVisibility("newCategoryInput","hide");
    modifyElementVisibility("saveCategoryButton","hide");
    modifyElementVisibility("discardButton","hide");
}
export function setInputModeVisibility(){
    modifyElementVisibility("newCategoryButton","hide");
    modifyElementVisibility("newCategoryInput","show");
    modifyElementVisibility("saveCategoryButton","show");
    modifyElementVisibility("discardButton","show");
}
export function modifyElementVisibility(elementId,showOrHide){

    let x = document.getElementById(elementId);
    if (showOrHide === "hide") {
      x.style.display = "none";
    } else {
      x.style.display = "initial";
    }
}