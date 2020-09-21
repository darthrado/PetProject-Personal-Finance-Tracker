export async function apiCallGetCategory(user,category){
    return fetch("/api/"+user+"/categories/"+category);
}
export async function apiCallSaveCategory(user,category){
    return fetch("/api/"+user+"/categories/"+category+"/saveNew", {method: 'POST'});
}
export async function apiCallEnableCategory(user,category){
    return fetch("/api/"+user+"/categories/"+category+"/enable", {method: 'PUT'});
}