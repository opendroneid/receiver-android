<?php

namespace App\Repositories;

use App\User;
use App\Interfaces\UserInterface;

class UserRepository implements UserInterface
{

    /**
     * create a new user
     *
     * @param string $name
     * @param string $email
     * @param string $password
     * @return User
     */
    public static function create(string $name, string $email, string $password): User
    {
        $user = User::create([
            'name' => $name,
            'email' => $email,
            'password' => bcrypt($password)
        ]);

        return $user;
    }

    /**
     * update user
     *
     * @param User $user
     * @param string $name
     * @return void
     */
    public static function update(User &$user, string $name)
    {
        $user->name = $name;
        $user->save();
    }

    /**
     * Verify a user
     *
     * @param User $user
     * @return void
     */
    public static function verify(User &$user)
    {
        $user->email_verified_at = new \DateTime();
        $user->save();
    }

    /**
     * Does this username exist?
     *
     * @param string $email
     * @return User|null
     */
    public static function findByEmail(string $email): ?User
    {
        try {
            $user = User::where('email', '=', $email)->firstOrFail();
            return $user;
        } catch (\Exception $e) {
            return null;
        }
    }

    /**
     * Has this username been taken?
     *
     * @param string $email
     * @return User|null
     */
    public static function emailTaken(string $email): ?User
    {
        try {
            
            $user = User::where('email', '=', $email)
                ->orWhere('email_temporary', '=', $email)
                ->firstOrFail();

            return $user;
        } catch (\Exception $e) {
            return null;
        }
    }

    /**
     * Set a users password
     *
     * @param User $user
     * @param string $password
     * @return void
     */
    public static function setPassword(User &$user, string $password)
    {
        $user->password = bcrypt($password);
        $user->save();
    }

    /**
     * Set a users email address temporariyl
     *
     * @param User $user
     * @param string $email
     * @return void
     */
    public static function setTempEmail(User &$user, string $email)
    {
        $user->email_temporary = $email;
        $user->save();
    }

    /**
     * Use temporary email address
     *
     * @param User $user
     * @param string $email
     * @return void
     */
    public static function confirmTempEmail(User &$user)
    {
        $user->email = $user->email_temporary;
        $user->email_temporary = null;
        $user->save();
    }

}