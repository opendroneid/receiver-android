<?php

namespace App\Repositories;

use DB;

use App\Plan;
use App\UserAsset;
use App\User;
use App\Interfaces\UserAssetInterface;
use Illuminate\Support\Facades\Log;

class UserAssetRepository implements UserAssetInterface
{

    /**
     * Get current user assets
     *
     * @param User $user
     * @return object
     */
    public static function findByUser(User $user): UserAsset
    {
        $userAsset = UserAsset::firstOrCreate([
            'user_id' => $user->id
        ]);

        return $userAsset;
    }

    /**
     * Create new user asset
     *
     * @param User $user
     * @param string $name
     * @param float $lat
     * @param float $lng
     * @return UserAsset
     */
    public static function create(User $user, string $name,float $lat, float $lng) : UserAsset
    {        
        $plan = Plan::where('id', '=', $user->userPlan()->plan_id)->firstOrFail();
        $radius = $plan->drone_detection_range;

        $position = self::getGeometry($lat, $lng);

        $asset = UserAsset::create([
            'user_id' => $user->id,
            'name' => $name,
            'lat' => $lat,
            'lng' => $lng,
            'position' =>  DB::raw($position),
            'radius' => $radius
        ]);

        return $asset;
    }

    /**
     * Get user assets
     *
     * @param User $user
     * @return object
     */
    public static function get(User $user): object
    {
        $assets = UserAsset::where('user_id', '=', $user->id)->get();
        return $assets;
    }
    
    /**
     * Has a user exceeded their asset number limit
     *
     * @param User $user
     * @return boolean
     */
    public static function hasExceededNumberLimit(User $user): bool
    {
        //number limit
        $limit = $user->userPlan()->max_asset_number;
        $assets = UserAsset::where('user_id', '=', $user->id)
        ->get();

        if (sizeof($assets) >= $limit) {
            return true;
        }

        return false;
    }

    /**
     * Get geomtry string for query
     *
     * @param float $lat
     * @param float $lng
     * @return string
     */
    private static function getGeometry( float $lat, float $lng): string
    {
        $positions = "ST_PointFromText('POINT({$lng} {$lat})')";
        return $positions;
    }

}
