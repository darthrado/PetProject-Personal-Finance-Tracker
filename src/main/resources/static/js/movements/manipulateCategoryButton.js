function onDocumentLoad(){
    setDefaultVisibility();
}

function newCategoryClick(user) {
    removeSuccessElements();
    setInputModeVisibility();
  }

async function saveCategoryClick(user) {
    const category = document.getElementById("newCategoryInput").value;

    const promise = await fetch("/"+user+"/api/categories/"+category);
    if(promise.status===404){
        await processCategoryNotExistsUseCase(category);
    }
    else if (promise.status===200){
        const responseJson = await promise.json();

        if(responseJson.flagActive === true){
            processCategoryActiveUseCase(category);
        }
        else{
            processCategoryInactiveUseCase(category);
        }
    }
    else{
        //something went wrong
    }
  }

  function discardCategoryClick(user) {
    setDefaultVisibility();
    removeErrorElements();
  }


/*Visibility Functions*/
function setDefaultVisibility(){
    modifyElementVisibility("newCategoryButton","show");
    modifyElementVisibility("newCategoryInput","hide");
    modifyElementVisibility("saveCategoryButton","hide");
    modifyElementVisibility("discardButton","hide");
}
function setInputModeVisibility(){
    modifyElementVisibility("newCategoryButton","hide");
    modifyElementVisibility("newCategoryInput","show");
    modifyElementVisibility("saveCategoryButton","show");
    modifyElementVisibility("discardButton","show");
}
function modifyElementVisibility(elementId,showOrHide){

    let x = document.getElementById(elementId);
    if (showOrHide === "hide") {
      x.style.display = "none";
    } else {
      x.style.display = "initial";
    }
}


/*Element creation and removal*/
function createErrorElement(errorMsg){
    let newElement = document.createElement("span");
    newElement.id="saveCategoryErr";
    newElement.classList.add("text-danger");
    newElement.textContent=errorMsg;

    return newElement;
}

function removeErrorElements(){
    let element = document.getElementById("saveCategoryErr");
    if(element!=undefined){
        element.remove();
    }
}


function createSuccessElement(successMsg){
    let newElement = document.createElement("span");
    newElement.id="saveCategorySuccess";
    newElement.classList.add("text-success");
    newElement.textContent=successMsg;

    return newElement;
}

function removeSuccessElements(){
    let element = document.getElementById("saveCategorySuccess");
    if(element!=undefined){
        element.remove();
    }
}


/*Server Requests*/
async function sendSaveCategoryRequest(category){
    console.log("Todo sending save request for"+category);
}

async function sendEnableCategoryRequest(category){
    console.log("Todo sending enable request for"+category);
}


async function processCategoryNotExistsUseCase(category){
    await sendSaveCategoryRequest(category);
    setDefaultVisibility();
    const successElement = createSuccessElement("Category Successfully Created");
    document.getElementById("categorySelectDiv").append(successElement);
}

async function processCategoryInactiveUseCase(category){

    const flagActivateUser = confirm(`Category ${category} already exists but inactive. Activate?`);

    if(flagActivateUser){
        await sendEnableCategoryRequest(category);
        const successElement = createSuccessElement("Category Enabled");
        document.getElementById("categorySelectDiv").append(successElement);
        setDefaultVisibility();
    }
    else{
        const errorElement = createErrorElement("Category already exists but inactive");
        document.getElementById("categorySelectDiv").append(errorElement);
    }


}

async function processCategoryActiveUseCase(category){
    const errorElement = createErrorElement("Category already exists");
    document.getElementById("categorySelectDiv").append(errorElement);
}

/*make modify dom on success method*/
/*on success add new category to list and select it*/
/*implement server calls for save and enable*/



/*use case 1: category exists, not disabled - error message*/
/*use case 2: category exists but disabled - prompt the user to enable it (4,5)*/
/*use case 3: category not exists - create it, go to 6*/
/*4: enable category then 6*/
/*5: disable category:*/
/*6: Add a li element in categories with the name and then select it. Restore new category componen to on load status*/



