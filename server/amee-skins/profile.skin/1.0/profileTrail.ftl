<p><a href='/profiles'>Profiles</a> / <a href='/profiles/${profile.displayPath}'>${profile.displayPath}</a><#list pathItem.pathItems as p><#if p.path != ''> / <a href='/profiles/${profile.displayPath}${p.fullPath}'>${p.name}</a></#if></#list></p>